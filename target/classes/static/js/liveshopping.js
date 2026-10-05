/**
 * JaHa Live Shopping JavaScript
 * Dynamic interactive features: Countdown Timer, 1-Click Order Modal, Quantity & Total Calculation
 */

document.addEventListener('DOMContentLoaded', function () {
    initFlashSaleCountdown();
    initQuickOrderModal();
    initCartDrawer();
});

/**
 * Flash Sale 24h Countdown Timer
 */
function initFlashSaleCountdown() {
    const hoursEl = document.getElementById('timerHours');
    const minsEl = document.getElementById('timerMins');
    const secsEl = document.getElementById('timerSecs');

    if (!hoursEl || !minsEl || !secsEl) return;

    // Set end of sale (e.g., midnight tonight)
    let now = new Date();
    let endOfDay = new Date();
    endOfDay.setHours(23, 59, 59, 999);

    let diffSecs = Math.max(0, Math.floor((endOfDay.getTime() - now.getTime()) / 1000));

    function updateTimer() {
        if (diffSecs <= 0) {
            diffSecs = 86400; // Reset for demo if hits zero
        }
        let h = Math.floor(diffSecs / 3600);
        let m = Math.floor((diffSecs % 3600) / 60);
        let s = diffSecs % 60;

        hoursEl.textContent = String(h).padStart(2, '0');
        minsEl.textContent = String(m).padStart(2, '0');
        secsEl.textContent = String(s).padStart(2, '0');

        diffSecs--;
    }

    updateTimer();
    setInterval(updateTimer, 1000);
}

/**
 * Bangladeshi Live Shopping 1-Click Order Modal ("অর্ডার করুন")
 */
let currentProduct = {
    id: null,
    name: '',
    price: 0,
    image: '',
    unit: 'pcs'
};

function initQuickOrderModal() {
    const modalEl = document.getElementById('quickOrderModal');
    if (!modalEl) return;

    const modal = new bootstrap.Modal(modalEl);

    // Attach click listeners to all "অর্ডার করুন" buttons
    document.querySelectorAll('.btn-quick-order-trigger').forEach(btn => {
        btn.addEventListener('click', function (e) {
            e.preventDefault();
            const id = this.getAttribute('data-product-id');
            const name = this.getAttribute('data-product-name');
            const price = parseFloat(this.getAttribute('data-product-price') || '0');
            const img = this.getAttribute('data-product-img');

            openQuickOrder(id, name, price, img, modal);
        });
    });

    // Quantity buttons inside modal
    const minusBtn = document.getElementById('modalQtyMinus');
    const plusBtn = document.getElementById('modalQtyPlus');
    const qtyInput = document.getElementById('modalQtyInput');

    if (minusBtn && plusBtn && qtyInput) {
        minusBtn.addEventListener('click', () => {
            let val = parseInt(qtyInput.value) || 1;
            if (val > 1) {
                qtyInput.value = val - 1;
                recalculateOrderModal();
            }
        });

        plusBtn.addEventListener('click', () => {
            let val = parseInt(qtyInput.value) || 1;
            qtyInput.value = val + 1;
            recalculateOrderModal();
        });

        qtyInput.addEventListener('change', () => {
            let val = parseInt(qtyInput.value) || 1;
            if (val < 1) val = 1;
            qtyInput.value = val;
            recalculateOrderModal();
        });
    }

    // Delivery zone radio toggle
    document.querySelectorAll('input[name="deliveryZone"]').forEach(radio => {
        radio.addEventListener('change', function () {
            document.querySelectorAll('.zone-option-card').forEach(c => c.classList.remove('active'));
            const parentCard = this.closest('.zone-option-card');
            if (parentCard) parentCard.classList.add('active');
            recalculateOrderModal();
        });
    });
}

function openQuickOrder(id, name, price, img, modalInstance) {
    currentProduct = { id, name, price, image: img };

    const idInput = document.getElementById('modalProductId');
    const nameEl = document.getElementById('modalProductName');
    const priceEl = document.getElementById('modalProductPrice');
    const imgEl = document.getElementById('modalProductImg');
    const qtyInput = document.getElementById('modalQtyInput');

    if (idInput) idInput.value = id;
    if (nameEl) nameEl.textContent = name;
    if (priceEl) priceEl.textContent = '৳ ' + price.toLocaleString();
    if (imgEl && img) imgEl.src = img;
    if (qtyInput) qtyInput.value = 1;

    // Reset delivery zone to Dhaka default
    const dhakaRadio = document.getElementById('zoneDhaka');
    if (dhakaRadio) {
        dhakaRadio.checked = true;
        document.querySelectorAll('.zone-option-card').forEach(c => c.classList.remove('active'));
        const parentCard = dhakaRadio.closest('.zone-option-card');
        if (parentCard) parentCard.classList.add('active');
    }

    recalculateOrderModal();

    if (modalInstance) {
        modalInstance.show();
    }
}

function recalculateOrderModal() {
    const qtyInput = document.getElementById('modalQtyInput');
    const qty = parseInt(qtyInput ? qtyInput.value : '1') || 1;
    const subtotal = currentProduct.price * qty;

    const isOutsideDhaka = document.querySelector('input[name="deliveryZone"]:checked')?.value === 'OUTSIDE';
    const deliveryFee = isOutsideDhaka ? 120 : 60;
    const grandTotal = subtotal + deliveryFee;

    const subtotalEl = document.getElementById('modalSubtotal');
    const deliveryEl = document.getElementById('modalDeliveryFee');
    const grandTotalEl = document.getElementById('modalGrandTotal');
    const submitBtnText = document.getElementById('modalSubmitBtnText');

    if (subtotalEl) subtotalEl.textContent = '৳ ' + subtotal.toLocaleString();
    if (deliveryEl) deliveryEl.textContent = '৳ ' + deliveryFee.toLocaleString();
    if (grandTotalEl) grandTotalEl.textContent = '৳ ' + grandTotal.toLocaleString();
    if (submitBtnText) submitBtnText.textContent = 'অর্ডার কনফার্ম করুন • ৳ ' + grandTotal.toLocaleString();
}

/**
 * Slide-out Cart Drawer & Local Cart Management
 */
function initCartDrawer() {
    // Add to cart buttons
    document.querySelectorAll('.btn-add-cart-trigger').forEach(btn => {
        btn.addEventListener('click', function (e) {
            e.preventDefault();
            const id = this.getAttribute('data-product-id');
            const name = this.getAttribute('data-product-name');
            const price = parseFloat(this.getAttribute('data-product-price') || '0');
            const img = this.getAttribute('data-product-img');

            addToCart({ id, name, price, img, quantity: 1 });
            showToast('✅ "' + name + '" কার্টে যোগ করা হয়েছে!');
        });
    });

    updateCartBadge();
}

function getCart() {
    try {
        return JSON.parse(localStorage.getItem('jaha_cart') || '[]');
    } catch (e) {
        return [];
    }
}

function saveCart(cart) {
    localStorage.setItem('jaha_cart', JSON.stringify(cart));
    updateCartBadge();
}

function addToCart(item) {
    let cart = getCart();
    let existing = cart.find(x => x.id === item.id);
    if (existing) {
        existing.quantity += item.quantity;
    } else {
        cart.push(item);
    }
    saveCart(cart);
}

function updateCartBadge() {
    let cart = getCart();
    let count = cart.reduce((sum, item) => sum + item.quantity, 0);
    document.querySelectorAll('.cart-count-badge').forEach(badge => {
        badge.textContent = count;
        badge.style.display = count > 0 ? 'inline-block' : 'none';
    });
}

function showToast(message) {
    let container = document.getElementById('jahaToastContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'jahaToastContainer';
        container.style.position = 'fixed';
        container.style.bottom = '20px';
        container.style.left = '20px';
        container.style.zIndex = '9999';
        document.body.appendChild(container);
    }

    let toast = document.createElement('div');
    toast.className = 'bg-dark text-white px-4 py-2 rounded-3 shadow-lg mb-2';
    toast.style.borderLeft = '4px solid #ff3366';
    toast.style.fontSize = '0.9rem';
    toast.style.fontWeight = '600';
    toast.innerHTML = message;

    container.appendChild(toast);
    setTimeout(() => {
        toast.style.transition = 'opacity 0.4s ease';
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 400);
    }, 2800);
}
