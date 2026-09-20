const overlay = document.getElementById("modal-overlay");
const openButton = document.getElementById("open-modal-btn");
const closeButton = document.getElementById("close-modal-btn");

export function openModal() {
    overlay.classList.remove("hidden");
}

export function closeModal() {
    overlay.classList.add("hidden");
}

export function setupModal(onClose) {
    openButton.addEventListener("click", openModal);
    closeButton.addEventListener("click", onClose);

    overlay.addEventListener("click", function (event) {
        if (event.target == overlay) {
            onClose();
        }
    });

    document.addEventListener("keydown", function (event) {
        if (event.key == "Escape" && !overlay.classList.contains("hidden")) {
            onClose();
        }
    });
}
