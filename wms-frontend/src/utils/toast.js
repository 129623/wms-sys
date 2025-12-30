import { createVNode, render } from 'vue';
import Toast from '../components/Toast.vue';

const toastDiv = document.createElement('div');
toastDiv.id = 'toast-container';
document.body.appendChild(toastDiv);

let activeToast = null;

const show = (message, type = 'info', duration = 3000) => {
    // If there is an active toast, we might want to stack them, but for simplicity we'll replace or just append
    // Since we used fixed position top-right, stacking needs logic.
    // Let's implement a simple stack or just single instance for now to avoid overlap mess without proper stack manager.
    // "Premium" usually implies stacking, but single instance is "cleaner" for quick impl.
    // Actually, let's create a new container for each toast so they stack naturally? 
    // No, `fixed top: 20px` will overlap.
    // Let's just create a container that flex-columns them.

    // Revised approach: The container is fixed. Toasts are children.
    // But Vue `render` replaces content of container usually unless we manage slots.

    // Simpler approach for "quick fix":
    // Create a container div at top right.
    // Append new Toast instances to it.

    const container = document.getElementById('toast-container-fixed') || createContainer();

    const div = document.createElement('div');
    container.appendChild(div);

    const onClose = () => {
        // Unmount and remove
        render(null, div);
        div.remove();
    };

    const vnode = createVNode(Toast, { message, type, duration, onClose });
    render(vnode, div);
};

const createContainer = () => {
    const div = document.createElement('div');
    div.id = 'toast-container-fixed';
    Object.assign(div.style, {
        position: 'fixed',
        top: '20px',
        right: '20px',
        zIndex: 9999,
        display: 'flex',
        flexDirection: 'column',
        gap: '10px',
        pointerEvents: 'none' // Allow clicks to pass through empty space
    });
    document.body.appendChild(div);
    return div;
};

// Add pointer-events auto to the toast wrapper in css so interactions work.

export const showToast = (message, type = 'info', duration = 3000) => {
    show(message, type, duration);
};

export default {
    success: (msg, duration) => show(msg, 'success', duration),
    error: (msg, duration) => show(msg, 'error', duration),
    warning: (msg, duration) => show(msg, 'warning', duration),
    info: (msg, duration) => show(msg, 'info', duration),
    show: showToast  // Also add it to default export
};
