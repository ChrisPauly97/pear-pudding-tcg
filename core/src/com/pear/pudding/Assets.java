//package com.pear.pudding;
//
//import com.badlogic.gdx.Gdx;
//import com.badlogic.gdx.assets.AssetDescriptor;
//import com.badlogic.gdx.assets.AssetLoaderParameters;
//import com.badlogic.gdx.assets.AssetManager;
//import com.badlogic.gdx.assets.loaders.FileHandleResolver;
//import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
//import com.badlogic.gdx.files.FileHandle;
//import com.badlogic.gdx.graphics.Texture;
//import com.badlogic.gdx.graphics.g2d.BitmapFont;
//import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
//import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
//import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
//import com.badlogic.gdx.scenes.scene2d.ui.Skin;
//
//public class Assets {
//    public AssetManager manager = new AssetManager();
//
//    ////////// Skins //////////
////        public static final AssetDescriptor<Skin> SKIN = new AssetDescriptor<Skin>("skins/default.json", Skin.class);
//    public static final AssetDescriptor<Skin> SKIN = new AssetDescriptor<Skin>("uiskin.json", Skin.class);
//
//    ////////// Textures //////////
//    public static final AssetDescriptor<Texture> GHOST = new AssetDescriptor<>("ghost.png", Texture.class);
//    public static final AssetDescriptor<Texture> CARDBACK = new AssetDescriptor<>("cardback.jpg", Texture.class);
//    public static final AssetDescriptor<Texture> CARD = new AssetDescriptor<>("card.png", Texture.class);
//    public static final AssetDescriptor<Texture> BACKGROUND = new AssetDescriptor<>("background.png", Texture.class);
//    public static final AssetDescriptor<Texture> HERO1 = new AssetDescriptor<>("ghost.png", Texture.class);
//    public static final AssetDescriptor<Texture> HERO2 = new AssetDescriptor<>("ghost.png", Texture.class);
////        public static final AssetDescriptor<Texture> TEST_TEXTURE_BLOCK = new AssetDescriptor<>(
////                "graphics/block/sample.png", Texture.class);
//
//    ////////// Fonts //////////
//    // In this project I was using FreeTypeFont, but using normal BitmapFonts is fine as well!
//    ////////// Music //////////
////        public static final AssetDescriptor<Music> INTRO_MUSIC = new AssetDescriptor<Music>("audio/music/intro.mp3",
////                Music.class);
//
//    ////////// SHADERS //////////
////        public static final ShaderProgram DEFAULT_SHADER = SpriteBatch.createDefaultShader();
//    public void load() {
//        try {
//            manager.load(SKIN);
//            manager.load(GHOST);
//            manager.load(CARDBACK);
//            manager.load(BACKGROUND);
//            manager.load(HERO1);
//            manager.load(HERO2);
//            manager.load(CARD);
//            FileHandleResolver resolver = new InternalFileHandleResolver();
//            manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
//            manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));
//            FreetypeFontLoader.FreeTypeFontLoaderParameter params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
//            params.fontParameters.size = 30;
//            params.fontFileName = "fonts/Satoshi-Variable.ttf";
//            manager.load("fonts/Satoshi-Variable.ttf", BitmapFont.class, params);
//        }catch (Exception e){
//            Gdx.app.log("Failed to load assets", e.toString());
//        }
//    }
//
//
//    public void dispose() {
//        manager.dispose();
//    }
//}
//
//
