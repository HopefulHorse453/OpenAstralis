package cc.astralis.ui.imgui.windows;

import cc.astralis.Astralis;
import imgui.ImGui;
import imgui.flag.ImGuiSelectableFlags;
import imgui.flag.ImGuiWindowFlags;
import cc.astralis.ui.imgui.ImGuiImpl;
import lombok.Getter;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

public class FileExplorerWindow {
    private String currentPath;
    private final String fileExtensionFilter;

    @Getter
    private String selectedFile;

    @Getter
    private boolean showConfirmationPopup = false;
    private String pendingSelection = null;

    private final List<FileEntry> directoryContents = new ArrayList<>();
    private final Stack<String> backStack = new Stack<>();
    private final Stack<String> forwardStack = new Stack<>();

    @Getter
    private boolean shouldClose = false;

    private static class FileEntry {
        final String name;
        final boolean isDirectory;
        final Path fullPath;
        final char icon;
        final String extension;

        FileEntry(String name, boolean isDirectory, Path fullPath) {
            this.name = name;
            this.isDirectory = isDirectory;
            this.fullPath = fullPath;
            this.extension = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
            this.icon = determineIcon(extension);
        }

        private char determineIcon(String extension) {
            if (isDirectory) return ImGuiImpl.ICON_FOLDER;

            return switch (extension) {
                case "png", "jpg", "jpeg", "gif" -> ImGuiImpl.ICON_IMAGE;
                case "txt", "pdf", "doc", "docx" -> ImGuiImpl.ICON_DOCUMENT;
                case "mp3", "wav", "ogg" -> ImGuiImpl.ICON_AUDIO;
                case "mp4", "mov", "avi" -> ImGuiImpl.ICON_VIDEO;
                case "zip", "rar", "7z" -> ImGuiImpl.ICON_ARCHIVE;
                default -> ImGuiImpl.ICON_FILE;
            };
        }
    }

    public FileExplorerWindow(String initialPath, String fileExtensionFilter) {
        this.currentPath = Paths.get(initialPath).toAbsolutePath().toString();
        this.fileExtensionFilter = fileExtensionFilter;
        refreshDirectoryContents();
    }

    public void showInline() {
        ImGui.text("Current Path: " + currentPath);
        ImGui.separator();

        ImGui.beginDisabled(backStack.isEmpty());
        if (ImGui.button("Back")) {
            navigateBack();
        }
        ImGui.endDisabled();

        ImGui.sameLine();

        ImGui.beginDisabled(forwardStack.isEmpty());
        if (ImGui.button("Forward")) {
            navigateForward();
        }
        ImGui.endDisabled();

        ImGui.sameLine();

        if (ImGui.button("Up") && !currentPath.equals("/")) {
            navigateUp();
        }

        ImGui.sameLine();
        if (ImGui.button("Refresh")) {
            refreshDirectoryContents();
        }

        ImGui.separator();

        if (ImGui.beginChild("Directory Contents", 0, 300, true)) {
            List<FileEntry> entries = new ArrayList<>(directoryContents);

            for (FileEntry entry : entries) {
                if (!fileExtensionFilter.equalsIgnoreCase(entry.extension) && !entry.isDirectory && !fileExtensionFilter.isBlank())
                    continue;

                ImGuiImpl.pushFontAwesome();

                ImGui.text(String.valueOf(entry.icon));
                ImGui.sameLine();

                ImGuiImpl.popFontAwesome();

                if (entry.isDirectory) {
                    if (ImGui.selectable(entry.name + "/", false, ImGuiSelectableFlags.DontClosePopups)) {
                        navigateTo(entry.fullPath.toString());
                    }
                } else {
                    if (ImGui.selectable(entry.name, Objects.equals(selectedFile, entry.fullPath.toString()))) {
                        pendingSelection = entry.fullPath.toString();
                        showConfirmationPopup = true;
                    }
                }
            }
            ImGui.endChild();
        }

        if (showConfirmationPopup) {
            ImGui.openPopup("Confirm Selection");
            ImGui.setNextWindowSize(400, 140);

            if (ImGui.beginPopupModal("Confirm Selection", ImGuiWindowFlags.NoResize)) {
                ImGui.text("Confirm selection of:");
                ImGui.textWrapped(pendingSelection);

                ImGui.dummy(0, 5);
                ImGui.separator();
                ImGui.dummy(0, 5);

                if (ImGui.button("Confirm", 120, 0)) {
                    selectedFile = pendingSelection;
                    shouldClose = true;
                    showConfirmationPopup = false;
                    ImGui.closeCurrentPopup();
                }

                ImGui.sameLine();
                if (ImGui.button("Cancel", 120, 0)) {
                    pendingSelection = null;
                    showConfirmationPopup = false;
                    ImGui.closeCurrentPopup();
                }

                ImGui.endPopup();
            }
        }
    }

    private void navigateTo(String newPath) {
        Path normalizedPath = Paths.get(newPath).normalize().toAbsolutePath();
        if (!normalizedPath.toString().equals(currentPath)) {
            backStack.push(currentPath);
            forwardStack.clear();
            currentPath = normalizedPath.toString();
            Astralis.LOGGER.error(currentPath);
            refreshDirectoryContents();
        }
    }

    private void navigateBack() {
        if (!backStack.isEmpty()) {
            forwardStack.push(currentPath);
            currentPath = backStack.pop();
            refreshDirectoryContents();
        }
    }

    private void navigateForward() {
        if (!forwardStack.isEmpty()) {
            backStack.push(currentPath);
            currentPath = forwardStack.pop();
            refreshDirectoryContents();
        }
    }

    private void navigateUp() {
        Path parentPath = Paths.get(currentPath).getParent();
        if (parentPath != null) {
            navigateTo(parentPath.toString());
        }
    }

    private synchronized void refreshDirectoryContents() {
        List<FileEntry> newContents = new ArrayList<>();
        selectedFile = null;

        File dir = new File(currentPath);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        newContents.add(new FileEntry(
                                file.getName(),
                                true,
                                file.toPath()
                        ));
                    }
                }
                for (File file : files) {
                    if (!file.isDirectory()) {
                        newContents.add(new FileEntry(
                                file.getName(),
                                false,
                                file.toPath()
                        ));
                    }
                }
            }
        }

        directoryContents.clear();
        directoryContents.addAll(newContents);
    }

    public void reset() {
        shouldClose = false;
        selectedFile = null;
    }
}