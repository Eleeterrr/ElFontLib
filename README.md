# ElFontLib

A Java text rendering library for LWJGL/OpenGL projects, built for MSDF fonts with full color emoji support (static and animated).

I built this originally as part of my game engine **Unify**, but it grew into its own thing and now it's what I use for text rendering in my mod **Sky Bubble**.

## What it does

- Renders text using MSDF (multi-channel signed distance field) fonts.
- Shapes text into glyph layouts and generates mesh data (vertices + indices) for OpenGL.
- Renders color emoji next to text, matched by unicode codepoint or by `:shortcode:`.
- Also handles animated emoji (GIF/WebP/APNG frames), with its own atlas, frame clock, and mesh generator.
- Builds emoji atlases from a folder of SVGs (or animated sources), packs them, caches the result, only rebuilds when the source files change.
- Has a file watcher that rebuilds emoji atlases when the source folder changes, no restart needed.

## Modules

- `font`: the `Font` interface and the MSDF font implementation (`MsdfFont`, `MsdfFontData`, `MsdfGlyph`).
- `shaping`: turns a string + font into a `TextLayout` of positioned glyphs (`TextShaper`, `SimpleTextShaper`).
- `render`: turns a layout into raw mesh data you can throw into a VBO (`TextMeshGenerator`, `MeshData`).
- `emoji`: static emoji: atlas data, glyph lookup, text scanning (splitting a string into text + emoji segments), layout, and mesh generation.
- `emoji.build`: builds an emoji atlas from a folder of SVGs, including rasterizing and packing.
- `emoji.animated`: same idea but for animated emoji: decodes GIF/WebP/APNG frames, builds an animated atlas, and hands you a frame clock to drive playback.
- `emoji.watch`: filesystem watcher that rebuilds atlases automatically when your emoji source folder changes.

## Basic usage

Loading a font and shaping some text:

```java
MsdfFontData fontData = MsdfJsonParser.load("font.json");
Font font = new MsdfFont(fontData);

TextShaper shaper = new SimpleTextShaper();
TextLayout layout = shaper.shape("Hello world", font, 32f);

MeshData mesh = TextMeshGenerator.generate(layout, font);
```

Mixing in emoji:

```java
EmojiFont emojiFont = EmojiAtlasBuilder.buildOrLoad("emoji_svgs/", "emoji_cache/");

CompositeTextRenderer renderer = new CompositeTextRenderer(font, emojiFont);
CompositeMesh mesh = renderer.render("Hello :wave: world", 32f);
```

`CompositeMesh` gives you back separate mesh data for text and emoji, since they'll usually be drawn with different textures/shaders.

## Building an emoji atlas

Drop a folder of SVGs somewhere and point the builder at it:

```java
EmojiBuildConfig config = new EmojiBuildConfig("svgs/", "cache/");
config.atlasCellSize = 256;
config.distanceRange = 4.0f;

EmojiFont emojiFont = EmojiAtlasBuilder.buildOrLoad(config);
```

It hashes the source files and skips the rebuild if nothing changed.

Animated emoji work the same way through `AnimatedEmojiAtlasBuilder` and `AnimatedBuildConfig`, just pointed at a folder of GIFs/WebPs instead of SVGs.

## Hot reload

If you want emoji atlases to rebuild automatically while you're iterating on assets:

```java
EmojiHotReloadWatcher watcher = new EmojiHotReloadWatcher(
    "svgs/", "cache/",
    "animated_source/", "animated_cache/",
    new EmojiHotReloadWatcher.Listener() {
        public void onStaticEmojiFontUpdated(EmojiFont newFont) { /* swap it in */ }
        public void onAnimatedEmojiFontUpdated(AnimatedEmojiFont newFont) { /* swap it in */ }
        public void onBuildError(Throwable error, String context) { /* log it */ }
    }
);
```

## Dependencies

- LWJGL 3.4 (glfw, opengl, stb, nanovg)
- JOML
- Gson
- TwelveMonkeys ImageIO (for WebP)

## Limitations

Shaping only handles Latin and Cyrillic. No Arabic, no Chinese, no complex script shaping, no RTL, no ligatures. Just left-to-right cursor advance line by line.

I don't really care about adding more than that. Built this for my own use, not for public use. Feel free to add it yourself if you want.

Font loading is MSDF-only.

## License

GPL-3.0, see `LICENSE`.
