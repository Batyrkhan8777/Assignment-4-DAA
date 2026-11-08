#!/usr/bin/env python3
"""Render the experiment CSV files as dependency-free SVG charts."""

import csv
import html
import sys
from pathlib import Path


COLORS = {
    "LCS": "#2563eb",
    "Edit distance": "#dc2626",
    "Naive": "#7c3aed",
    "KMP": "#059669",
    "Rabin-Karp": "#ea580c",
    "Kasai": "#0891b2",
}


def read_csv(path):
    with path.open(newline="", encoding="utf-8") as stream:
        return list(csv.DictReader(stream))


def chart(path, title, x_label, y_label, series):
    width, height = 920, 560
    left, right, top, bottom = 92, 35, 64, 105
    plot_w, plot_h = width - left - right, height - top - bottom
    all_x = [x for item in series for x, _ in item["points"]]
    all_y = [y for item in series for _, y in item["points"]]
    x_min, x_max = min(all_x), max(all_x)
    y_max = max(all_y) or 1.0
    y_max *= 1.12

    def px(value):
        if x_max == x_min:
            return left + plot_w / 2
        return left + (value - x_min) * plot_w / (x_max - x_min)

    def py(value):
        return top + plot_h - value * plot_h / y_max

    chunks = [
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" viewBox="0 0 {width} {height}">',
        '<rect width="100%" height="100%" fill="#ffffff"/>',
        '<style>text{font-family:Arial,Helvetica,sans-serif;fill:#1f2937}.grid{stroke:#e5e7eb;stroke-width:1}.axis{stroke:#374151;stroke-width:1.5}</style>',
        f'<text x="{width / 2}" y="32" text-anchor="middle" font-size="22" font-weight="700">{html.escape(title)}</text>',
    ]
    for tick in range(6):
        value = y_max * tick / 5
        y = py(value)
        chunks.append(f'<line class="grid" x1="{left}" y1="{y:.1f}" x2="{left + plot_w}" y2="{y:.1f}"/>')
        chunks.append(f'<text x="{left - 12}" y="{y + 5:.1f}" text-anchor="end" font-size="12">{value:.3g}</text>')
    chunks.extend([
        f'<line class="axis" x1="{left}" y1="{top}" x2="{left}" y2="{top + plot_h}"/>',
        f'<line class="axis" x1="{left}" y1="{top + plot_h}" x2="{left + plot_w}" y2="{top + plot_h}"/>',
        f'<text x="{left + plot_w / 2}" y="{height - 24}" text-anchor="middle" font-size="15">{html.escape(x_label)}</text>',
        f'<text x="24" y="{top + plot_h / 2}" text-anchor="middle" font-size="15" transform="rotate(-90 24 {top + plot_h / 2})">{html.escape(y_label)}</text>',
    ])
    for x in sorted(set(all_x)):
        x_pos = px(x)
        chunks.append(f'<line class="axis" x1="{x_pos:.1f}" y1="{top + plot_h}" x2="{x_pos:.1f}" y2="{top + plot_h + 5}"/>')
        chunks.append(f'<text x="{x_pos:.1f}" y="{top + plot_h + 23}" text-anchor="middle" font-size="12">{x:g}</text>')
    for item in series:
        points = " ".join(f"{px(x):.1f},{py(y):.1f}" for x, y in item["points"])
        color = COLORS.get(item["label"], "#4b5563")
        chunks.append(f'<polyline points="{points}" fill="none" stroke="{color}" stroke-width="3" stroke-linejoin="round" stroke-linecap="round"/>')
        for x, y in item["points"]:
            chunks.append(f'<circle cx="{px(x):.1f}" cy="{py(y):.1f}" r="5" fill="{color}"/>')

    legend_y = height - 58
    total_legend_w = sum(38 + len(item["label"]) * 8 + 22 for item in series)
    legend_x = max(left, (width - total_legend_w) / 2)
    for item in series:
        color = COLORS.get(item["label"], "#4b5563")
        chunks.append(f'<line x1="{legend_x}" y1="{legend_y}" x2="{legend_x + 27}" y2="{legend_y}" stroke="{color}" stroke-width="3"/>')
        chunks.append(f'<text x="{legend_x + 34}" y="{legend_y + 5}" font-size="13">{html.escape(item["label"])}</text>')
        legend_x += 38 + len(item["label"]) * 8 + 22
    chunks.append('</svg>')
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text("\n".join(chunks) + "\n", encoding="utf-8")


def main():
    results = Path(sys.argv[1] if len(sys.argv) > 1 else "results")
    dp_rows = read_csv(results / "dynamic-programming.csv")
    dp_series = []
    for algorithm in ("LCS", "Edit distance"):
        rows = [row for row in dp_rows if row["algorithm"] == algorithm]
        dp_series.append({
            "label": algorithm,
            "points": [(int(row["input_size"]), float(row["average_ns"]) / 1_000_000) for row in rows],
        })
    chart(results / "plots" / "dynamic-programming.svg", "Dynamic programming runtime", "String length (each input)", "Average time (ms)", dp_series)

    matching_rows = read_csv(results / "string-matching.csv")
    for scenario in ("present", "absent"):
        series = []
        for algorithm in ("Naive", "KMP", "Rabin-Karp"):
            rows = [row for row in matching_rows if row["scenario"] == scenario and row["algorithm"] == algorithm]
            series.append({
                "label": algorithm,
                "points": [(int(row["text_length"]), float(row["average_ns"]) / 1_000_000) for row in rows],
            })
        chart(results / "plots" / f"string-matching-{scenario}.svg", f"String matching runtime ({scenario} pattern)", "Text length", "Average time (ms)", series)

    kasai_rows = read_csv(results / "kasai.csv")
    chart(results / "plots" / "kasai.svg", "Kasai LCP construction runtime", "String length", "Average time (ms)", [{
        "label": "Kasai",
        "points": [(int(row["input_size"]), float(row["average_ns"]) / 1_000_000) for row in kasai_rows],
    }])


if __name__ == "__main__":
    main()
