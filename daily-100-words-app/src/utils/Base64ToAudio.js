import { useCallback } from "react";

export function speakJapanese(base64Audio) {
  // 1) Convert Base64 → binary
  const byteCharacters = atob(base64Audio);
  const byteNumbers = new Array(byteCharacters.length);
  for (let i = 0; i < byteCharacters.length; i++) {
    byteNumbers[i] = byteCharacters.charCodeAt(i);
  }
  const byteArray = new Uint8Array(byteNumbers);

  // 2) Make Blob with correct MIME type
  const blob = new Blob([byteArray], { type: "audio/wav" });

  // 3) Create a temporary object URL
  const url = URL.createObjectURL(blob);

  // 4) Play
  const audio = new Audio(url);
  audio.play();
}
