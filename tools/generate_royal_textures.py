#!/usr/bin/env python3
"""
Generates the royal theme's texture assets.

These are not clip art. A Persian kashi pattern is a geometric construction, so
it is constructed here rather than drawn by hand or downloaded: the eight-point
star and the cross that fills the space between stars come out of the same
square lattice, which is why the result tiles seamlessly at any size.

The grain textures are built in the frequency domain. Noise made that way is
periodic by definition, so it tiles with no seam to hide.

Run from the repo root:  python3 tools/generate_royal_textures.py
Writes into app/src/main/res/drawable-nodpi/.
"""

import math
import pathlib

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

OUT = pathlib.Path(__file__).resolve().parent.parent / "app/src/main/res/drawable-nodpi"
SUPERSAMPLE = 4


def octagram_points(cx, cy, radius, points=8):
    """Vertices of an eight-pointed star, outer and inner alternating."""
    inner = radius * math.sin(math.pi / points) / math.sin(math.pi - 3 * math.pi / points)
    step = math.pi / points
    out = []
    for k in range(points * 2):
        r = radius if k % 2 == 0 else inner
        angle = k * step
        out.append((cx + r * math.cos(angle), cy + r * math.sin(angle)))
    return out


def kashi_tile(size=256, line=3.0, radius_ratio=0.46, corner_ratio=0.20, alpha=1.0):
    """
    One cell of a star-and-knot kashi pattern, drawn as gold line work on
    transparency so it can be tinted and laid over any ground.

    The star sits at the centre of the cell and is large enough that its points
    almost reach the cell edge, so neighbouring stars interlock. At each cell
    corner sits a small rotated square; the four diagonal star points that meet
    there are tied to its vertices, and the four cells sharing that corner turn
    it into the knot node that holds the field together.

    The pattern is symmetric about both cell edges, so it tiles with no seam.
    """
    s = size * SUPERSAMPLE
    img = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    width = max(1, int(round(line * SUPERSAMPLE)))
    ink = (255, 255, 255, 255)

    cx = cy = s / 2
    radius = s * radius_ratio
    corner = s * corner_ratio

    star = octagram_points(cx, cy, radius)
    draw.line(star + [star[0]], fill=ink, width=width, joint="curve")

    for ox, oy in ((0, 0), (s, 0), (0, s), (s, s)):
        node = [
            (ox - corner, oy), (ox, oy - corner),
            (ox + corner, oy), (ox, oy + corner),
        ]
        draw.line(node + [node[0]], fill=ink, width=width, joint="curve")

    # Tie each diagonal star point to the two nearest vertices of its corner node.
    for k in (1, 3, 5, 7):
        angle = k * math.pi / 4
        px, py = cx + radius * math.cos(angle), cy + radius * math.sin(angle)
        ox = 0 if math.cos(angle) < 0 else s
        oy = 0 if math.sin(angle) < 0 else s
        draw.line([(px, py), (ox + (corner if math.cos(angle) < 0 else -corner), oy)],
                  fill=ink, width=width)
        draw.line([(px, py), (ox, oy + (corner if math.sin(angle) < 0 else -corner))],
                  fill=ink, width=width)

    img = img.resize((size, size), Image.LANCZOS)
    if alpha < 1.0:
        # Bake the opacity into the file. android:alpha on <bitmap> only exists
        # from API 31, and this app still ships to API 21.
        channel = img.getchannel("A").point(lambda v: int(v * alpha))
        img.putalpha(channel)
    return img


def periodic_noise(size, octaves, seed):
    """
    Tileable noise: a random spectrum with 1/f falloff, inverse-transformed.
    Because the transform is discrete, the result wraps exactly.
    """
    rng = np.random.default_rng(seed)
    field = np.zeros((size, size), dtype=np.float64)
    for octave, weight in octaves:
        spectrum = np.zeros((size, size), dtype=complex)
        fy = np.fft.fftfreq(size) * size
        fx = np.fft.fftfreq(size) * size
        radius = np.hypot(*np.meshgrid(fx, fy, indexing="ij"))
        mask = (radius > 0) & (radius < octave)
        phase = rng.uniform(0, 2 * math.pi, (size, size))
        spectrum[mask] = np.exp(1j * phase[mask]) / radius[mask]
        layer = np.real(np.fft.ifft2(spectrum))
        peak = np.abs(layer).max()
        if peak > 0:
            field += weight * layer / peak
    peak = np.abs(field).max()
    return field / peak if peak > 0 else field


def foil_grain(size=128, seed=7):
    """
    The faint tooth of hammered gold leaf. Kept as a grey alpha mask so one
    texture can sit over any gold gradient without fighting its hue.
    """
    base = periodic_noise(size, [(size * 0.45, 1.0), (size * 0.18, 0.5)], seed)
    # Stretch horizontally so the grain reads as brushed metal, not static.
    streaks = periodic_noise(size, [(size * 0.5, 1.0)], seed + 1)
    field = 0.65 * base + 0.35 * np.repeat(streaks[:, :1], size, axis=1)
    alpha = np.clip((field * 0.5 + 0.5), 0, 1)
    alpha = (alpha * 46).astype(np.uint8)  # very light: texture, not pattern
    img = Image.fromarray(alpha, mode="L")
    rgba = Image.new("RGBA", (size, size), (255, 255, 255, 0))
    rgba.putalpha(img)
    return rgba


def parchment_grain(size=192, seed=23):
    """Paper tooth for panel interiors — coarser and softer than the foil."""
    field = periodic_noise(size, [(size * 0.30, 1.0), (size * 0.10, 0.6), (size * 0.05, 0.4)], seed)
    alpha = np.clip(field * 0.5 + 0.5, 0, 1)
    alpha = (alpha * 34).astype(np.uint8)
    rgba = Image.new("RGBA", (size, size), (255, 255, 255, 0))
    rgba.putalpha(Image.fromarray(alpha, mode="L").filter(ImageFilter.GaussianBlur(0.6)))
    return rgba


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    written = []

    for name, image in (
        # Two weights of the same field: a quiet one for full-screen grounds and
        # a slightly stronger one for panels, where it sits behind less content.
        ("tex_kashi.png", kashi_tile(alpha=0.11)),
        ("tex_kashi_panel.png", kashi_tile(size=192, line=2.4, alpha=0.17)),
        ("tex_foil_grain.png", foil_grain()),
        ("tex_parchment_grain.png", parchment_grain()),
    ):
        path = OUT / name
        image.save(path, optimize=True)
        written.append((name, path.stat().st_size))

    for name, size in written:
        print(f"{name:26} {size / 1024:6.1f} KB")


if __name__ == "__main__":
    main()
