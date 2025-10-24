// Laboratory 2: Structural Design Pattern
// Modular Media Streaming Suite (All-in-one Java Version)
// Patterns used: Adapter, Composite, Decorator, Strategy, Proxy (conceptual)

import java.io.*;
import java.util.*;

// === Base Media Structures ===
class MediaChunk {
    byte[] data;
    Map<String, String> meta;

    MediaChunk(byte[] data, Map<String, String> meta) {
        this.data = data;
        this.meta = meta != null ? meta : new HashMap<>();
    }
}

// === Media Source Abstraction ===
interface IMediaSource {
    Iterable<MediaChunk> stream();
}

// === Adapter Pattern ===
// Adapts different media sources (File, HLS, Remote API) into a unified interface
class FileSource implements IMediaSource {
    private String path;

    FileSource(String path) { this.path = path; }

    public Iterable<MediaChunk> stream() {
        List<MediaChunk> chunks = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(path)) {
            byte[] buf = new byte[1024];
            int n;
            while ((n = fis.read(buf)) != -1) {
                byte[] chunkData = Arrays.copyOf(buf, n);
                Map<String, String> meta = new HashMap<>();
                meta.put("source", path);
                chunks.add(new MediaChunk(chunkData, meta));
            }
        } catch (IOException e) {
            System.out.println("[FileSource] Error: " + e.getMessage());
        }
        return chunks;
    }
}

class HlsSource implements IMediaSource {
    private List<byte[]> segments;

    HlsSource(List<byte[]> segments) { this.segments = segments; }

    public Iterable<MediaChunk> stream() {
        List<MediaChunk> out = new ArrayList<>();
        for (int i = 0; i < segments.size(); i++) {
            Map<String, String> meta = new HashMap<>();
            meta.put("segment", "HLS-" + i);
            out.add(new MediaChunk(segments.get(i), meta));
        }
        return out;
    }
}

// Simulated Remote API Source (Proxy-like behaviour)
class RemoteApiSource implements IMediaSource {
    interface Fetcher { byte[] fetch(); }
    private Fetcher fetcher;

    RemoteApiSource(Fetcher fetcher) { this.fetcher = fetcher; }

    public Iterable<MediaChunk> stream() {
        List<MediaChunk> chunks = new ArrayList<>();
        byte[] data = fetcher.fetch();
        int pos = 0, chunkSize = 2048;
        while (pos < data.length) {
            int end = Math.min(pos + chunkSize, data.length);
            byte[] sub = Arrays.copyOfRange(data, pos, end);
            Map<String, String> meta = new HashMap<>();
            meta.put("remote", "true");
            chunks.add(new MediaChunk(sub, meta));
            pos = end;
        }
        return chunks;
    }
}

// === Strategy Pattern ===
// Allows runtime switching of rendering strategy
interface Renderer {
    void start();
    void render(byte[] data, Map<String,String> meta);
    void stop();
}

class SoftwareRenderer implements Renderer {
    public void start() { System.out.println("[Renderer] Software start"); }
    public void render(byte[] data, Map<String,String> meta) {
        System.out.println("[SoftwareRenderer] Rendering " + data.length + " bytes " + meta);
    }
    public void stop() { System.out.println("[Renderer] Software stop"); }
}

class HardwareRenderer implements Renderer {
    public void start() { System.out.println("[Renderer] Hardware start"); }
    public void render(byte[] data, Map<String,String> meta) {
        System.out.println("[HardwareRenderer] Rendering " + data.length + " bytes " + meta);
    }
    public void stop() { System.out.println("[Renderer] Hardware stop"); }
}

// === Composite Pattern ===
// Allows playlists to contain single files or other playlists
interface MediaItem {
    Iterable<MediaChunk> stream();
}

class MediaFileItem implements MediaItem {
    IMediaSource source;
    String title;

    MediaFileItem(IMediaSource source, String title) {
        this.source = source; 
        this.title = title;
    }

    public Iterable<MediaChunk> stream() { return source.stream(); }
}

class Playlist implements MediaItem {
    List<MediaItem> items = new ArrayList<>();
    String title;

    Playlist(String title) { this.title = title; }

    void add(MediaItem item) { items.add(item); }

    public Iterable<MediaChunk> stream() {
        List<MediaChunk> all = new ArrayList<>();
        for (MediaItem item : items)
            for (MediaChunk c : item.stream())
                all.add(c);
        return all;
    }
}

// === Decorator Pattern ===
// Adds extra behaviours (plugins) such as subtitles or watermark
interface Plugin {
    RenderFunction wrap(RenderFunction inner);
}
interface RenderFunction { void render(byte[] data, Map<String,String> meta); }

class WatermarkPlugin implements Plugin {
    String text;
    WatermarkPlugin(String text) { this.text = text; }

    public RenderFunction wrap(RenderFunction inner) {
        return (data, meta) -> {
            meta.put("watermark", text);
            System.out.println("[Plugin] Watermark: " + text);
            inner.render(data, meta);
        };
    }
}

class SubtitlePlugin implements Plugin {
    String[] lines; 
    int idx = 0;
    SubtitlePlugin(String[] lines) { this.lines = lines; }

    public RenderFunction wrap(RenderFunction inner) {
        return (data, meta) -> {
            System.out.println("[Plugin] Subtitle: " + lines[idx % lines.length]);
            idx++;
            inner.render(data, meta);
        };
    }
}

// === Main Application Entry ===
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Modular Media Streaming Suite Demo ===\n");

        // Create media sources
        IMediaSource local = new FileSource("example.mp3");
        IMediaSource hls = new HlsSource(Arrays.asList("seg1".getBytes(), "seg2".getBytes()));
        IMediaSource remote = new RemoteApiSource(() -> "RemoteStreamData".getBytes());

        // Build composite playlist
        Playlist playlist = new Playlist("My Playlist");
        playlist.add(new MediaFileItem(local, "Local File"));
        playlist.add(new MediaFileItem(hls, "HLS Stream"));
        playlist.add(new MediaFileItem(remote, "Remote API"));

        // Choose rendering strategy
        Renderer renderer = new SoftwareRenderer();
        renderer.start();

        // Create and apply plugins (decorators)
        RenderFunction baseRender = renderer::render;
        Plugin watermark = new WatermarkPlugin("© Doona Studio");
        Plugin subtitles = new SubtitlePlugin(new String[]{"Hello!", "Enjoy the show!"});

        // Stack the decorators
        RenderFunction pluginStack = subtitles.wrap(watermark.wrap(baseRender));

        // Render all chunks from the playlist
        for (MediaChunk chunk : playlist.stream()) {
            pluginStack.render(chunk.data, chunk.meta);
        }

        renderer.stop();

        System.out.println("\n=== Switching Renderer to Hardware ===");
        renderer = new HardwareRenderer();
        renderer.start();
        for (MediaChunk chunk : playlist.stream()) {
            renderer.render(chunk.data, chunk.meta);
        }
        renderer.stop();

        System.out.println("\n=== Demo Complete ===");
    }
}