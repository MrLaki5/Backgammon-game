#!/usr/bin/env python3
"""
Composites the menu and journey screens from the app's real assets.

This is a proofing tool, not a build step. It draws with the same PNGs, the same
nine-patches and the same font the app ships, at the same proportions, so a look
at its output is a look at the screen — as opposed to an HTML mock-up, which can
flatter a design the device would not.

It does not replace running the app. It catches the things that are settled
before a device ever sees them: whether the type sits well on the panel, whether
the gold has enough contrast against the wood, whether a row of controls is
balanced.

Run from the repo root:  python3 tools/preview_screens.py
"""

import pathlib

import arabic_reshaper
from bidi.algorithm import get_display
from PIL import Image, ImageDraw, ImageFont, features

ROOT = pathlib.Path(__file__).resolve().parent.parent
RES = ROOT / "app/src/main/res"
XHDPI = RES / "drawable-xhdpi"
NODPI = RES / "drawable-nodpi"
FONTS = RES / "font"
OUT = ROOT / "build/previews"

# Defaults are a common landscape phone at xhdpi. Override to proof a specific
# handset — a layout that fits 640x360dp can still overflow a shorter one, and
# that is a class of bug only a second size catches.
#   RB_W / RB_H  device pixels, landscape
#   RB_DP        density (px per dp)
import os

W = int(os.environ.get("RB_W", 1280))
H = int(os.environ.get("RB_H", 720))
DP = float(os.environ.get("RB_DP", 2))


def dp(value):
    return int(round(value * DP))


def fa(text):
    """
    Pillow is built with libraqm here, so it shapes and reorders Persian itself.
    Reshaping first would apply the transformation twice and draw the line
    backwards, which is exactly what the first run of this tool did.
    """
    if not features.check("raqm"):
        return get_display(arabic_reshaper.reshape(text))
    return text


FA_XHDPI = RES / "drawable-fa-xhdpi"


def font(weight, size_sp):
    path = {
        "regular": FONTS / "vazirmatn_regular.ttf",
        "semibold": FONTS / "vazirmatn_semibold.ttf",
        "extrabold": FONTS / "vazirmatn_extrabold.ttf",
        "display": FONTS / "lalezar_regular.ttf",
        "story": FONTS / "amiri_bold.ttf",
    }[weight]
    return ImageFont.truetype(str(path), dp(size_sp))


def plate(name, height_dp):
    """A struck title, scaled to the height the layout gives it."""
    art = Image.open(FA_XHDPI / f"{name}.png").convert("RGBA")
    height = dp(height_dp)
    width = max(1, round(art.width * height / art.height))
    return art.resize((width, height), Image.LANCZOS)


def nine_patch_stretch(path, width, height):
    """
    Scale a nine-patch the way Android does: the marked band stretches, the rest
    keeps its pixels. The assets mark one band in each axis, so this splits into
    three slices per axis and resizes only the middle one.
    """
    src = Image.open(path).convert("RGBA")
    marks_x = [x for x in range(src.width) if src.getpixel((x, 0))[3] > 0]
    marks_y = [y for y in range(src.height) if src.getpixel((0, y))[3] > 0]
    body = src.crop((1, 1, src.width - 1, src.height - 1))
    sx0, sx1 = marks_x[0] - 1, marks_x[-1]
    sy0, sy1 = marks_y[0] - 1, marks_y[-1]

    left, right = sx0, body.width - sx1
    top, bottom = sy0, body.height - sy1
    mid_w, mid_h = max(width - left - right, 1), max(height - top - bottom, 1)

    out = Image.new("RGBA", (width, height), (0, 0, 0, 0))

    def piece(box, size, dest):
        out.paste(body.crop(box).resize(size, Image.LANCZOS), dest)

    piece((0, 0, sx0, sy0), (left, top), (0, 0))
    piece((sx1, 0, body.width, sy0), (right, top), (width - right, 0))
    piece((0, sy1, sx0, body.height), (left, bottom), (0, height - bottom))
    piece((sx1, sy1, body.width, body.height), (right, bottom), (width - right, height - bottom))
    piece((sx0, 0, sx1, sy0), (mid_w, top), (left, 0))
    piece((sx0, sy1, sx1, body.height), (mid_w, bottom), (left, height - bottom))
    piece((0, sy0, sx0, sy1), (left, mid_h), (0, top))
    piece((sx1, sy0, body.width, sy1), (right, mid_h), (width - right, top))
    piece((sx0, sy0, sx1, sy1), (mid_w, mid_h), (left, top))
    return out


def backdrop():
    """bg_royal_backdrop.xml: board art, kashi field, then a wash."""
    base = Image.open(NODPI / "meny_background.webp").convert("RGBA").resize((W, H), Image.LANCZOS)

    tile = Image.open(NODPI / "tex_kashi.png").convert("RGBA")
    gold = Image.new("RGBA", tile.size, (224, 174, 76, 255))
    gold.putalpha(tile.getchannel("A"))
    field = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    for y in range(0, H, tile.height):
        for x in range(0, W, tile.width):
            field.alpha_composite(gold, (x, y))
    base.alpha_composite(field)

    wash = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    draw = ImageDraw.Draw(wash)
    for y in range(H):
        t = y / H
        alpha = int(179 * (1 - t) + 204 * t) if t > 0.5 else int(179 - 64 * (t * 2))
        draw.line([(0, y), (W, y)], fill=(21, 13, 6, alpha))
    base.alpha_composite(wash)
    return base


def vector_icon(name, rgb, size):
    """
    Rasterise one of the app's vector drawables. Android's <path pathData>
    is SVG path syntax, so the drawable can be wrapped in an <svg> element and
    rendered directly — which keeps this preview honest, rather than standing in
    a circle where the app draws an icon.
    """
    import re
    import cairosvg

    source = (RES / "drawable" / f"{name}.xml").read_text()
    paths = re.findall(r'android:pathData="([^"]+)"', source)
    viewport = re.search(r'android:viewportWidth="([\d.]+)"', source)
    box = float(viewport.group(1)) if viewport else 24.0
    fill = "#%02X%02X%02X" % rgb
    body = "".join(f'<path d="{d}" fill="{fill}"/>' for d in paths)
    svg = (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {box} {box}" '
           f'width="{size[0]}" height="{size[1]}">{body}</svg>')
    png = cairosvg.svg2png(bytestring=svg.encode(), output_width=size[0], output_height=size[1])
    import io
    return Image.open(io.BytesIO(png)).convert("RGBA")


def tinted(path, rgb, size):
    icon = Image.open(path).convert("RGBA").resize(size, Image.LANCZOS)
    solid = Image.new("RGBA", size, rgb + (255,))
    solid.putalpha(icon.getchannel("A"))
    return solid


GOLD_BRIGHT = (247, 212, 136)
GOLD = (224, 174, 76)
PARCHMENT = (244, 233, 208)
INK_SOFT = (226, 207, 172)
INK_MUTED = (169, 143, 108)
INK_ON_GOLD = (35, 21, 4)
TURQUOISE = (52, 197, 172)


def centred(draw, box, text, fnt, fill):
    x0, y0, x1, y1 = box
    w = draw.textlength(text, font=fnt)
    ascent, descent = fnt.getmetrics()
    draw.text((x0 + (x1 - x0 - w) / 2, y0 + (y1 - y0 - ascent - descent) / 2 + descent / 2),
              text, font=fnt, fill=fill)


def menu_screen():
    canvas = backdrop()
    draw = ImageDraw.Draw(canvas)

    # Panel on the leading edge, which in Persian is the right.
    pw, ph = dp(404), dp(330)
    px, py = W - pw - dp(20), H - ph - dp(12)
    canvas.alpha_composite(nine_patch_stretch(XHDPI / "bg_royal_panel.9.png", pw, ph), (px, py))

    cursor = py + dp(14)
    cornice = Image.open(XHDPI / "muqarnas_cornice.png").convert("RGBA")
    cornice = cornice.resize((pw - dp(48), dp(32)), Image.LANCZOS)
    canvas.alpha_composite(cornice, (px + dp(24), cursor - dp(6)))
    cursor += dp(32)

    title = plate("title_app", 44)
    canvas.alpha_composite(title, (px + (pw - title.width) // 2, cursor))
    cursor += dp(50)
    centred(draw, (px, cursor, px + pw, cursor + dp(20)),
            fa("کلاسیک، اما امروزی"), font("story", 14), INK_SOFT)
    cursor += dp(26)
    centred(draw, (px, cursor, px + pw, cursor + dp(18)),
            fa("چالش امروز: پلاکوتو"), font("regular", 11.5), TURQUOISE)
    cursor += dp(24)

    hero_w, hero_h = dp(240), dp(56)
    hx = px + (pw - hero_w) // 2
    canvas.alpha_composite(nine_patch_stretch(XHDPI / "btn_gold.9.png", hero_w, hero_h), (hx, cursor))
    label = fa("بازی")
    fnt = font("semibold", 19)
    label_w = draw.textlength(label, font=fnt)
    icon = vector_icon("ic_royal_play", INK_ON_GOLD, (dp(22), dp(22)))
    group_w = label_w + dp(9) + icon.width
    gx = hx + (hero_w - group_w) / 2
    draw.text((gx, cursor + hero_h / 2 - dp(12)), label, font=fnt, fill=INK_ON_GOLD)
    canvas.alpha_composite(icon, (int(gx + label_w + dp(9)), cursor + (hero_h - icon.height) // 2))
    cursor += hero_h + dp(12)

    rows = [
        [("دو نفره", "gold_duel"), ("سفر", "gold_road"), ("آموزش", "gold_book")],
        [("امتیازها", "gold_trophy"), ("فروشگاه", "gold_shop"), ("تنظیمات", "gold_gear")],
    ]
    gap, tile_h = dp(8), dp(44)
    tile_w = (pw - dp(48) - gap * 2) // 3
    for row in rows:
        for i, (label, icon_name) in enumerate(reversed(row)):
            tx = px + dp(24) + i * (tile_w + gap)
            canvas.alpha_composite(nine_patch_stretch(XHDPI / "btn_wood.9.png", tile_w, tile_h),
                                   (tx, cursor))
            icon = Image.open(XHDPI / f"{icon_name}.png").convert("RGBA")
            icon = icon.resize((dp(23), dp(23)), Image.LANCZOS)
            # Persian reads right to left, so the icon sits on the right.
            canvas.alpha_composite(icon, (tx + tile_w - dp(8) - icon.width,
                                          cursor + (tile_h - icon.height) // 2))
            fnt = font("display", 13)
            draw.text((tx + tile_w - dp(8) - icon.width - dp(8)
                       - draw.textlength(fa(label), font=fnt),
                       cursor + tile_h / 2 - dp(9)),
                      fa(label), font=fnt, fill=GOLD_BRIGHT)
        cursor += tile_h + gap

    # Crest on the trailing edge.
    logo = Image.open(NODPI / "royal_backgammon_logo.webp").convert("RGBA")
    logo.thumbnail((dp(176), dp(160)), Image.LANCZOS)
    canvas.alpha_composite(logo, (dp(40), (H - logo.height) // 2))

    # Top bar badges.
    x = W - dp(18)
    for text, width in ((fa("۱٬۲۴۰"), dp(92)), (fa("سکهٔ رایگان"), dp(104))):
        x -= width
        art = "btn_wood.9.png" if "۱" in text else "btn_gold.9.png"
        canvas.alpha_composite(nine_patch_stretch(XHDPI / art, width, dp(32)), (x, dp(8)))
        centred(draw, (x, dp(8), x + width, dp(40)), text, font("semibold", 12),
                GOLD_BRIGHT if art.startswith("btn_wood") else INK_ON_GOLD)
        x -= dp(8)
    return canvas


def journey_screen():
    canvas = backdrop()
    draw = ImageDraw.Draw(canvas)

    panel_w = int(W * 0.28)
    scrim = Image.new("RGBA", (panel_w, H), (21, 13, 6, 194))
    canvas.alpha_composite(scrim, (W - panel_w, 0))

    right = W - dp(18)
    journey_title = plate("title_journey", 38)
    canvas.alpha_composite(journey_title, (right - journey_title.width, dp(50)))
    draw.text((right - draw.textlength(fa("فصل‌های تمام‌شده"), font=font("regular", 13)), dp(150)),
              fa("فصل‌های تمام‌شده"), font=font("regular", 13), fill=INK_MUTED)
    draw.text((right - draw.textlength(fa("فصل بعدی: آتن"), font=font("regular", 13)), dp(210)),
              fa("فصل بعدی: آتن"), font=font("regular", 13), fill=INK_MUTED)

    cards = [
        ("قهوه‌خانه", "حریف: استاد کاظم", "۶۰ سکه", 2, "done"),
        ("استانبول", "حریف: امره", "۸۰ سکه", 3, "current"),
        ("آتن", "حریف: نیکوس", "۱۰۰ سکه", 4, "locked"),
        ("کرت", "حریف: النی", "۱۲۰ سکه", 5, "locked"),
    ]
    card_w, card_h = dp(156), dp(168)
    x = W - panel_w - dp(18) - card_w
    for title, opponent, reward, chapter, state in cards:
        y = (H - card_h) // 2
        art = nine_patch_stretch(XHDPI / "btn_wood.9.png", card_w, card_h)
        glaze = Image.new("RGBA", (card_w, card_h), {
            "done": (46, 158, 140, 26),
            "current": (224, 174, 76, 38),
            "locked": (21, 13, 6, 166),
        }[state])
        art.alpha_composite(glaze)
        canvas.alpha_composite(art, (x, y))

        emblem = Image.open(XHDPI / f"emblem_chapter_{chapter}.png").convert("RGBA")
        emblem = emblem.resize((dp(42), dp(42)), Image.LANCZOS)
        canvas.alpha_composite(emblem, (x + card_w - dp(11) - dp(42), y + dp(10)))
        centred(draw, (x + card_w - dp(11) - dp(42), y + dp(10),
                       x + card_w - dp(14), y + dp(12) + dp(48)),
                fa(str(chapter)), font("extrabold", 15), PARCHMENT)

        ty = y + dp(60)
        for text, weight, size, colour in (
            (title, "display", 17, PARCHMENT if state != "locked" else INK_MUTED),
            (opponent, "regular", 13, INK_SOFT if state != "locked" else INK_MUTED),
        ):
            fnt = font(weight, size)
            draw.text((x + card_w - dp(11) - draw.textlength(fa(text), font=fnt), ty),
                      fa(text), font=fnt, fill=colour)
            ty += dp(size + 8)

        fnt = font("semibold", 12.5)
        draw.text((x + card_w - dp(11) - draw.textlength(fa(reward), font=fnt), y + card_h - dp(30)),
                  fa(reward), font=fnt, fill=GOLD_BRIGHT)

        x -= card_w + dp(30)
        if x + card_w < 0:
            break
    return canvas


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    for name, screen in (("menu.png", menu_screen()), ("journey.png", journey_screen())):
        path = OUT / name
        screen.convert("RGB").save(path, quality=92)
        print(path)


if __name__ == "__main__":
    main()
