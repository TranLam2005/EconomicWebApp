const LongWishlistPage = (() => {
    function setStatus(message, type = "info") {
        const status = document.getElementById("wishlistStatus");
        if (!status) return;
        const colorClass = type === "error"
            ? "border-red-200 bg-red-50 text-red-700"
            : type === "success"
                ? "border-emerald-200 bg-emerald-50 text-emerald-700"
                : "border-stone-200 bg-stone-50 text-stone-700";
        status.className = `mt-6 rounded-2xl border px-5 py-4 text-sm font-semibold ${colorClass}`;
        status.textContent = message;
    }

    async function loadWishlist() {
        const grid = document.getElementById("wishlistGrid");
        const count = document.getElementById("wishlistCount");
        const empty = document.getElementById("wishlistEmpty");

        if (grid) {
            grid.innerHTML = "<div class='col-span-full rounded-3xl border border-dashed border-stone-300 bg-white p-8 text-center text-stone-500'>Đang tải sản phẩm yêu thích...</div>";
        }

        const response = await fetch("/api/favorites", {headers: LongShop.headers(false)});
        if (!response.ok) {
            const text = await response.text();
            setStatus(`Không tải được danh sách yêu thích: ${response.status} - ${text}`, "error");
            return;
        }

        const data = await response.json();
        if (count) count.textContent = data.length;

        if (!data.length) {
            if (grid) grid.innerHTML = "";
            if (empty) empty.classList.remove("hidden");
            setStatus("Danh sách yêu thích đang rỗng.", "info");
            return;
        }

        if (empty) empty.classList.add("hidden");
        if (grid) grid.innerHTML = data.map(item => renderFavorite(item)).join("");
        setStatus("Đã tải danh sách yêu thích.", "success");
    }

    function renderFavorite(item) {
        return `
            <article class="group overflow-hidden rounded-[2rem] border border-stone-200 bg-white shadow-sm transition hover:-translate-y-1 hover:shadow-xl">
                <div class="relative aspect-[4/5] overflow-hidden bg-stone-100">
                    <img src="${LongShop.imageUrl(item.imageUrl)}" alt="${LongShop.escapeHtml(item.productName)}" class="h-full w-full object-cover transition duration-500 group-hover:scale-105">
                    <button onclick="LongWishlistPage.remove(${item.productId})" class="absolute right-4 top-4 flex h-12 w-12 items-center justify-center rounded-full bg-white/95 text-xl text-red-500 shadow-lg transition hover:scale-105" aria-label="Xóa yêu thích">♥</button>
                    <div class="absolute bottom-4 left-4 rounded-full bg-white/95 px-4 py-2 text-xs font-bold uppercase tracking-[0.18em] text-[#005b4b] shadow">${LongShop.escapeHtml(item.brand || "Parfumerie")}</div>
                </div>
                <div class="p-5">
                    <div class="mb-3 flex flex-wrap gap-2 text-xs text-stone-500">
                        <span class="rounded-full bg-stone-100 px-3 py-1">${LongShop.escapeHtml(item.gender || "Unisex")}</span>
                        <span class="rounded-full bg-stone-100 px-3 py-1">${LongShop.escapeHtml(item.concentration || "EDP")}</span>
                    </div>
                    <h3 class="line-clamp-2 min-h-14 text-lg font-bold text-stone-950">${LongShop.escapeHtml(item.productName)}</h3>
                    <p class="mt-3 text-xl font-extrabold text-red-600">${LongShop.formatCurrency(item.price)}</p>
                    <div class="mt-5 grid gap-3 sm:grid-cols-2">
                        <button onclick="LongWishlistPage.addToCart(${item.productId})" class="rounded-full bg-[#005b4b] px-4 py-3 text-sm font-bold text-white transition hover:bg-stone-950">Thêm giỏ</button>
                        <a href="/search?q=${encodeURIComponent(item.productName || "")}" class="rounded-full border border-stone-200 px-4 py-3 text-center text-sm font-bold text-stone-800 transition hover:border-[#005b4b] hover:text-[#005b4b]">Tìm tương tự</a>
                    </div>
                </div>
            </article>
        `;
    }

    async function remove(productId) {
        const ok = await LongShop.removeFavorite(productId);
        if (ok) await loadWishlist();
    }

    async function addToCart(productId) {
        const variantId = await LongShop.getFirstVariantId(productId);
        const ok = await LongShop.addToCart(variantId, 1);
        if (ok) {
            LongShop.toast("Đã thêm sản phẩm yêu thích vào giỏ hàng.");
        }
    }

    async function addManualFavorite() {
        const input = document.getElementById("wishlistProductInput");
        const productId = Number(input && input.value ? input.value : 0);
        if (!productId) {
            setStatus("Bạn cần nhập Product ID để thêm sản phẩm yêu thích.", "error");
            return;
        }

        const ok = await LongShop.addFavorite(productId);
        if (ok) await loadWishlist();
    }

    document.addEventListener("DOMContentLoaded", loadWishlist);

    return {loadWishlist, remove, addToCart, addManualFavorite};
})();
