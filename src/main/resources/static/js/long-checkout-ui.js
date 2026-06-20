const LongCheckoutPage = (() => {
    let cartItems = [];
    let subtotalAmount = 0;
    let shippingAmount = 0;
    const FREE_SHIPPING_MIN = 1000000;
    const SHIPPING_FEE = 35000;

    function $(id) {
        return document.getElementById(id);
    }

    function setMessage(message, type = "info") {
        const box = $("checkoutMessage");
        if (!box) return;
        box.textContent = message || "";
        box.className = "mt-5 rounded border px-5 py-4 text-sm font-semibold " + (
            type === "error" ? "border-red-200 bg-red-50 text-red-700" :
            type === "success" ? "border-emerald-200 bg-emerald-50 text-emerald-700" :
            type === "warning" ? "border-amber-200 bg-amber-50 text-amber-800" :
            "border-stone-200 bg-stone-50 text-stone-700"
        );
        box.style.display = message ? "block" : "none";
    }

    function isLoggedIn() {
        return !!(window.ParfumerieAuth && window.ParfumerieAuth.getAccessToken && window.ParfumerieAuth.getAccessToken());
    }

    function fillCustomerInfo() {
        const form = $("checkoutForm");
        if (!form) return;
        const email = window.ParfumerieAuth && window.ParfumerieAuth.getUserEmail
            ? window.ParfumerieAuth.getUserEmail()
            : localStorage.getItem("parfumerie_user_email");
        const name = localStorage.getItem("parfumerie_user_name") || "";
        if (email && form.customerEmail) form.customerEmail.value = email;
        if (name && form.customerName && !form.customerName.value) form.customerName.value = name;

        const notice = $("checkoutLoginNotice");
        if (notice) {
            notice.classList.toggle("hidden", isLoggedIn());
        }
    }

    async function loadCart() {
        const list = $("checkoutItemsList");
        if (list) {
            list.innerHTML = `<div class="rounded border border-dashed border-stone-300 bg-white p-5 text-center text-stone-500">Đang tải giỏ hàng...</div>`;
        }

        try {
            const response = await fetch("/api/cart", { headers: LongShop.headers(false) });
            if (!response.ok) {
                const text = await response.text();
                throw new Error(`${response.status} - ${text}`);
            }

            const data = await response.json();
            cartItems = data.items || [];
            subtotalAmount = Number(data.totalAmount || 0);
            shippingAmount = subtotalAmount > 0 && subtotalAmount < FREE_SHIPPING_MIN ? SHIPPING_FEE : 0;

            renderCart();
        } catch (error) {
            if (list) {
                list.innerHTML = `<div class="rounded border border-red-200 bg-red-50 p-5 text-sm text-red-700">Không tải được giỏ hàng: ${LongShop.escapeHtml(error.message)}</div>`;
            }
            setMessage("Không tải được giỏ hàng. Bạn hãy đăng nhập lại hoặc quay về giỏ hàng kiểm tra.", "error");
        }
    }

    function renderCart() {
        const list = $("checkoutItemsList");
        const count = cartItems.reduce((sum, item) => sum + Number(item.quantity || 0), 0);
        const grandTotal = subtotalAmount + shippingAmount;

        if ($("checkoutTotalItems")) $("checkoutTotalItems").textContent = count;
        if ($("checkoutSubtotal")) $("checkoutSubtotal").textContent = LongShop.formatCurrency(subtotalAmount);
        if ($("checkoutShipping")) $("checkoutShipping").textContent = shippingAmount > 0 ? LongShop.formatCurrency(shippingAmount) : "-";
        if ($("checkoutGrandTotal")) $("checkoutGrandTotal").textContent = LongShop.formatCurrency(grandTotal);

        const shippingNotice = $("shippingNotice");
        if (shippingNotice) {
            shippingNotice.textContent = subtotalAmount >= FREE_SHIPPING_MIN
                ? "Đơn hàng của bạn được miễn phí vận chuyển."
                : "Phí vận chuyển tạm tính là " + LongShop.formatCurrency(SHIPPING_FEE) + ".";
        }

        if (!cartItems.length) {
            if (list) {
                list.innerHTML = `<div class="rounded border border-dashed border-stone-300 bg-white p-5 text-center text-stone-500">Giỏ hàng đang trống. <a href="/tim-kiem" class="font-bold text-[#005b4b] underline">Tìm sản phẩm</a></div>`;
            }
            const button = $("placeOrderButton");
            if (button) button.disabled = true;
            return;
        }

        const button = $("placeOrderButton");
        if (button) button.disabled = false;

        if (list) {
            list.innerHTML = cartItems.map(item => `
                <article class="flex gap-3">
                    <div class="relative h-16 w-16 shrink-0 overflow-hidden rounded border border-stone-200 bg-white">
                        <img src="${LongShop.imageUrl(item.imageUrl)}" alt="${LongShop.escapeHtml(item.productName)}" class="h-full w-full object-cover">
                        <span class="absolute -right-1 -top-1 flex h-5 w-5 items-center justify-center rounded-full bg-[#005b4b] text-xs font-bold text-white">${Number(item.quantity || 1)}</span>
                    </div>
                    <div class="min-w-0 flex-1">
                        <h3 class="truncate font-semibold text-stone-800">${LongShop.escapeHtml(item.productName)}</h3>
                        <p class="mt-1 text-xs text-stone-500">${LongShop.escapeHtml(item.brand || "Parfumerie")} / ${LongShop.escapeHtml(item.variantName || "")}</p>
                    </div>
                    <b class="shrink-0 text-sm text-stone-600">${LongShop.formatCurrency(item.lineTotal || item.unitPrice || 0)}</b>
                </article>
            `).join("");
        }
    }

    function buildShippingAddress(form) {
        const parts = [
            form.shippingAddress.value,
            form.ward.value,
            form.district.value,
            form.city.value
        ].map(value => (value || "").trim()).filter(Boolean);
        return parts.join(", ");
    }

    async function placeOrder(event) {
        event.preventDefault();
        const form = event.currentTarget;
        const button = $("placeOrderButton");

        if (!cartItems.length) {
            setMessage("Giỏ hàng đang trống, không thể đặt hàng.", "error");
            return;
        }
        if (!$("checkoutPolicy") || !$("checkoutPolicy").checked) {
            setMessage("Bạn cần đồng ý với chính sách bán hàng trước khi đặt hàng.", "error");
            return;
        }
        if (!form.customerEmail.value.trim() || !form.customerName.value.trim() || !form.customerPhone.value.trim() || !form.shippingAddress.value.trim()) {
            setMessage("Vui lòng nhập đầy đủ email, họ tên, số điện thoại và địa chỉ.", "error");
            return;
        }

        const paymentMethod = form.paymentMethod.value || "COD";
        const payload = {
            items: cartItems.map(item => ({
                productId: Number(item.productId),
                quantity: Number(item.quantity || 1)
            })),
            customerName: form.customerName.value.trim(),
            customerEmail: form.customerEmail.value.trim(),
            customerPhone: form.customerPhone.value.trim(),
            shippingAddress: buildShippingAddress(form),
            shippingFee: shippingAmount,
            subtotalAmount: subtotalAmount,
            discountCode: ($("checkoutDiscount") && $("checkoutDiscount").value.trim()) || null,
            note: form.note.value.trim(),
            paymentMethod: paymentMethod
        };

        try {
            if (button) {
                button.disabled = true;
                button.textContent = "ĐANG ĐẶT...";
            }
            setMessage("Đang tạo đơn hàng...", "info");

            const response = await fetch("/public/api/order/create", {
                method: "POST",
                headers: LongShop.headers(true),
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                const text = await response.text();
                throw new Error(text || "Tạo đơn hàng thất bại.");
            }

            const data = await response.json().catch(() => ({}));
            await fetch("/api/cart", { method: "DELETE", headers: LongShop.headers(false) }).catch(() => null);

            if (data.paymentUrl) {
                setMessage("Tạo đơn hàng thành công. Đang chuyển sang trang thanh toán...", "success");
                window.location.href = data.paymentUrl;
                return;
            }

            setMessage("Đặt hàng thành công. Đang chuyển sang trang đơn hàng của bạn...", "success");
            cartItems = [];
            subtotalAmount = 0;
            shippingAmount = 0;
            renderCart();
            setTimeout(function () {
                window.location.href = "/don-hang?success=1";
            }, 700);
        } catch (error) {
            const message = error && error.message ? error.message : "Đặt hàng thất bại. Vui lòng kiểm tra lại thông tin.";
            setMessage("Đặt hàng thất bại: " + message, "error");
        } finally {
            if (button) {
                button.disabled = false;
                button.textContent = "ĐẶT HÀNG";
            }
        }
    }

    document.addEventListener("DOMContentLoaded", () => {
        fillCustomerInfo();
        loadCart();
        const form = $("checkoutForm");
        if (form) form.addEventListener("submit", placeOrder);
        const discountButton = $("checkoutApplyDiscount");
        if (discountButton) {
            discountButton.addEventListener("click", () => setMessage("Mã giảm giá sẽ được kiểm tra khi bấm ĐẶT HÀNG.", "info"));
        }
    });

    return { loadCart, placeOrder };
})();
