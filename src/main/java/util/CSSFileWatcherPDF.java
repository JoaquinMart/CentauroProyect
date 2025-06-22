package util;

import javafx.application.Platform;
import javafx.scene.Scene;
import controllers.PDFController;

import java.io.IOException;
import java.nio.file.*;

public class CSSFileWatcherPDF extends Thread {

    private Path cssPath;
    private Scene targetScene;

    // Constructor que recibe la ruta del archivo CSS
    public CSSFileWatcherPDF(String cssFilePath, Scene scene) {
        this.cssPath = Paths.get(cssFilePath);
        this.targetScene = scene;
        setDaemon(true);
    }

    @Override
    public void run() {
        try {
            // Crear un servicio para observar cambios en el sistema de archivos
            WatchService watchService = FileSystems.getDefault().newWatchService();

            // Registrar la carpeta que contiene el archivo CSS para escuchar cambios
            cssPath.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            while (true) {
                // Bloquea el hilo hasta que haya un evento de cambio en el archivo
                WatchKey key = watchService.take();

                for (WatchEvent<?> event : key.pollEvents()) {
                    // Verificar si el evento es una modificación del archivo CSS
                    if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                        String fileName = event.context().toString();
                        if (fileName.equals(cssPath.getFileName().toString())) {
                            // Usar Platform.runLater para asegurarnos de que la actualización
                            // de la interfaz se haga en el hilo de la interfaz de usuario (UI thread)
                            Platform.runLater(() -> {
                                // Usa la Scene objetivo directamente, sin depender de PDFController.getSecondaryStage()
                                targetScene.getStylesheets().clear();
                                targetScene.getStylesheets().add(cssPath.toUri().toString());
                                System.out.println("Estilos de PDF recargados desde: " + cssPath.toUri().toString());
                            });
                        }
                    }
                }

                // Reestablecer la clave para poder seguir observando eventos
                key.reset();
            }

        } catch (IOException | InterruptedException e) {
            // Manejo adecuado de las excepciones
            e.printStackTrace();
        }
    }
}
