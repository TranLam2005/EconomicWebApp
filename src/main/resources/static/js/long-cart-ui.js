const LongCartPage = (() => {
    function quantityValue() {
        const quantityInput = document.getElementById("cartQuantityInput");
        return Math.max(1, Number(quantityInput && quantityInput.value ? quantityInput.value : 1));
    }

    function setStatus(message, type = "info") {
        const status = document.getElementById("cartStatus");
        if (!status) return;
        const colorClass = type === "error"
            ? "border-red-200 bg-red-50 text-red-700"
            : type === "success"
                ? "border-emerald-200 bg-emerald-50 text-emerald-700"
                : "border-stone-200 bg-stone-50 text-stone-700";
        status.className = `mt-6 rounded-2xl border px-5 py-4 text-sm font-semibold ${colorClass}`;
        status.textContent = message;
    }

    async function loadCart() {
        const list = document.getElementById("cartItems");
        const empty = document.getElementById("cartEmpty");
        const totalQuantity = document.getElementById("cartTotalQuantity");
        const subtotal = document.getElementById("cartSubtotal");
        const grandTotal = document.getElementById("cartGrandTotal");
        const checkoutItems = document.getElementById("checkoutItems");

        if (list) {
            list.innerHTML = "<div class='rounded-3xl border border-dashed border-stone-300 bg-white p-8 text-center text-stone-500'>Đang tải giỏ hàng...</div>";
        }

        const response = await fetch("/api/cart", {headers: LongShop.headers(false)});
        if (!response.ok) {
            const text = await response.text();
            setStatus(`Không tải được giỏ hàng: ${response.status} - ${text}`, "error");
            return;
        }

        const data = await response.json();
        const items = data.items || [];
        const amount = Number(data.totalAmount || 0);
        const count = Number(data.totalQuantity || 0);

        if (totalQuantity) totalQuantity.textContent = count;
        if (window.LongHeaderCart && typeof window.LongHeaderCart.setBadge === "function") {
            window.LongHeaderCart.setBadge(count);
        }
        if (subtotal) subtotal.textContent = LongShop.formatCurrency(amount);
        if (grandTotal) grandTotal.textContent = LongShop.formatCurrency(amount);
        if (checkoutItems) checkoutItems.textContent = `${count} sản phẩm`;

        if (!items.length) {
            if (list) list.innerHTML = "";
            if (empty) empty.classList.remove("hidden");
            setStatus("Giỏ hàng đang rỗng.", "info");
            return;
        }

        if (empty) empty.classList.add("hidden");
        if (list) {
            list.innerHTML = items.map(item => renderItem(item)).join("");
        }
        setStatus("Đã tải giỏ hàng.", "success");
    }

    function renderItem(item) {
        return `
            <article class="group rounded-[2rem] border border-stone-200 bg-white p-4 shadow-sm transition hover:-translate-y-0.5 hover:shadow-xl sm:p-5">
                <div class="grid gap-5 lg:grid-cols-[140px_1fr_auto] lg:items-center">
                    <div class="aspect-square overflow-hidden rounded-3xl bg-stone-100">
                        <img src="${LongShop.imageUrl(item.imageUrl)}" alt="${LongShop.escapeHtml(item.productName)}" class="h-full w-full object-cover transition duration-500 group-hover:scale-105">
                    </div>
                    <div>
                        <div class="flex flex-wrap items-center gap-2 text-xs uppercase tracking-[0.2em] text-stone-400">
                            <span>${LongShop.escapeHtml(item.brand || "Parfumerie")}</span>
                            <span class="h-1 w-1 rounded-full bg-stone-300"></span>
                            <span>${LongShop.escapeHtml(item.variantName || "Dung tích tiêu chuẩn")}</span>
                        </div>
                        <h3 class="mt-2 text-xl font-bold text-stone-950">${LongShop.escapeHtml(item.productName)}</h3>
                        <p class="mt-2 text-sm text-stone-500">Variant #${item.variantId || "-"} • ${item.volumeMl || ""}ml • Còn ${item.stockQuantity ?? "-"} sản phẩm</p>
                        <div class="mt-4 flex flex-wrap items-center gap-3">
                            <span class="rounded-full bg-[#005b4b]/10 px-4 py-2 text-sm font-bold text-[#005b4b]">${LongShop.formatCurrency(item.unitPrice)}</span>
                            <span class="text-sm text-stone-500">Thành tiền: <b class="text-stone-900">${LongShop.formatCurrency(item.lineTotal)}</b></span>
                        </div>
                    </div>
                    <div class="flex flex-col gap-3 lg:min-w-52">
                        <div class="flex items-center justify-between rounded-full border border-stone-200 bg-stone-50 px-2 py-2">
                            <button onclick="LongCartPage.changeQuantity(${item.itemId}, ${Math.max(1, Number(item.quantity || 1) - 1)})" class="h-9 w-9 rounded-full bg-white text-xl shadow-sm hover:bg-stone-100">−</button>
                            <span class="px-4 text-lg font-bold">${item.quantity}</span>
                            <button onclick="LongCartPage.changeQuantity(${item.itemId}, ${Number(item.quantity || 1) + 1})" class="h-9 w-9 rounded-full bg-white text-xl shadow-sm hover:bg-stone-100">+</button>
                        </div>
                        <button onclick="LongCartPage.updateItem(${item.itemId})" class="rounded-full bg-stone-950 px-5 py-3 text-sm font-bold text-white transition hover:bg-[#005b4b]">Sửa theo ô số lượng</button>
                        <button onclick="LongCartPage.deleteItem(${item.itemId})" class="rounded-full border border-red-200 px-5 py-3 text-sm font-bold text-red-600 transition hover:bg-red-50">Xóa sản phẩm</button>
                    </div>
                </div>
            </article>
        `;
    }

    async function addByVariant() {
        const variantInput = document.getElementById("cartVariantInput");
        const variantId = Number(variantInput && variantInput.value ? variantInput.value : 0);
        const quantity = quantityValue();

        if (!variantId) {
            setStatus("Bạn cần nhập Variant ID để thêm sản phẩm vào giỏ.", "error");
            return;
        }

        const ok = await LongShop.addToCart(variantId, quantity);
        if (ok) {
            await loadCart();
        }
    }

    async function updateItem(itemId) {
        const quantity = quantityValue();
        const response = await fetch(`/api/cart/items/${itemId}`, {
            method: "PUT",
            headers: LongShop.headers(true),
            body: JSON.stringify({quantity})
        });

        if (!response.ok) {
            const text = await response.text();
            setStatus(`Sửa số lượng thất bại: ${response.status} - ${text}`, "error");
            return;
        }

        LongShop.toast("Đã cập nhật số lượng.");
        await loadCart();
    }

    async function changeQuantity(itemId, quantity) {
        const response = await fetch(`/api/cart/items/${itemId}`, {
            method: "PUT",
            headers: LongShop.headers(true),
            body: JSON.stringify({quantity: Math.max(1, Number(quantity || 1))})
        });

        if (!response.ok) {
            const text = await response.text();
            setStatus(`Sửa số lượng thất bại: ${response.status} - ${text}`, "error");
            return;
        }

        await loadCart();
    }

    async function deleteItem(itemId) {
        if (!confirm("Xóa sản phẩm này khỏi giỏ hàng?")) return;

        const response = await fetch(`/api/cart/items/${itemId}`, {
            method: "DELETE",
            headers: LongShop.headers(false)
        });

        if (!response.ok) {
            const text = await response.text();
            setStatus(`Xóa sản phẩm thất bại: ${response.status} - ${text}`, "error");
            return;
        }

        LongShop.toast("Đã xóa sản phẩm khỏi giỏ.");
        await loadCart();
    }

    async function clearCart() {
        if (!confirm("Xóa toàn bộ giỏ hàng?")) return;

        const response = await fetch("/api/cart", {
            method: "DELETE",
            headers: LongShop.headers(false)
        });

        if (!response.ok) {
            const text = await response.text();
            setStatus(`Xóa giỏ hàng thất bại: ${response.status} - ${text}`, "error");
            return;
        }

        LongShop.toast("Đã xóa toàn bộ giỏ hàng.");
        await loadCart();
    }

    document.addEventListener("DOMContentLoaded", loadCart);

    return {loadCart, addByVariant, updateItem, changeQuantity, deleteItem, clearCart};
})();
