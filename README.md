
# Modular Media Streaming Suite  
**Laboratory 2: Structural Design Pattern (Java Implementation)**  

This project evolves a simple media player into a **Modular Media Streaming Suite** using **Structural Design Patterns**.  
It demonstrates how to refactor a monolithic player into a flexible, extensible system that supports multiple media sources, plugins, playlists, rendering strategies, and remote caching simulation.  

---

# Features Implemented
- Multiple media sources: Local File, HLS Stream, Remote API  
- Composite Playlists (mix files and sub-playlists)  
- Decorator-based plugin system (e.g., Watermark, Subtitles)  
- Strategy Pattern for renderer switching (Hardware vs Software)  
- Proxy-like Remote API streaming (simulated caching behaviour)

---
README
How to Run the Project
1. Save the file as Main.java.
2. Open a terminal or command prompt in the project folder.
3. Compile the program:
javac Main.java
4. Run the demo:
java Main

The program will show:
Playlist creation and media playback
Plugins (subtitles and watermark) applied
Switching between software and hardware renderers

---
How to Test
To test different features:
Edit the renderer in Main.java (switch between SoftwareRenderer and HardwareRenderer).
Add or remove plugins (WatermarkPlugin, SubtitlePlugin).
Add more media sources or playlists to test composite behaviour.

---
Demo Command Summary:
javac Main.java
java Main
