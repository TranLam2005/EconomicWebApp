const LongSearchPage = (() => {
    function getValue(id) {
        const element = document.getElementById(id);
        return element && element.value ? element.value.trim() : "";
    }

    function activeSearchLabel() {
        const q = getValue("searchKeyword");
        const brand = getValue("searchBrand");
        const gender = getValue("searchGender");
        const concentration = getValue("searchConcentration");
        const parts = [];

        if (q) parts.push(q);
        if (brand) parts.push(`Thương hiệu ${brand}`);
        if (gender) parts.push(`Nước hoa ${gender}`);
        if (concentration) parts.push(concentration);

        return parts.length ? parts.join(" • ") : "tất cả sản phẩm";
    }

    function paramsFromForm() {
        const params = new URLSearchParams();
        const q = getValue("searchKeyword");
        const brand = getValue("searchBrand");
        const gender = getValue("searchGender");
        const concentration = getValue("searchConcentration");
        const minPrice = getValue("searchMinPrice");
        const maxPrice = getValue("searchMaxPrice");
        const sort = getValue("searchSort") || "latest";

        if (q) params.set("q", q);
        if (brand) params.set("brand", brand);
        if (gender) params.set("gender", gender);
        if (concentration) params.set("concentration", concentration);
        if (minPrice) params.set("minPrice", minPrice);
        if (maxPrice) params.set("maxPrice", maxPrice);
        params.set("sort", sort);
        params.set("limit", "80");
        return params;
    }

    async function searchProducts(pushState = true) {
        const grid = document.getElementById("searchGrid");
        const count = document.getElementById("searchCount");
        const keywordText = document.getElementById("searchKeywordText");
        const empty = document.getElementById("searchEmpty");
        const params = paramsFromForm();

        if (grid) {
            grid.innerHTML = "<div class='col-span-full rounded-3xl border border-dashed border-stone-300 bg-white p-8 text-center text-stone-500'>Đang tìm sản phẩm...</div>";
        }

        if (pushState) {
            const browserParams = new URLSearchParams(params);
            browserParams.delete("limit");
            const nextUrl = `${location.pathname}${browserParams.toString() ? `?${browserParams}` : ""}`;
            window.history.replaceState(null, "", nextUrl);
        }

        const response = await fetch(`/api/long/products?${params.toString()}`, {headers: LongShop.headers(false)});
        if (!response.ok) {
            const text = await response.text();
            LongShop.toast(`Tìm kiếm thất bại: ${response.status} - ${text}`, "error");
            return;
        }

        const data = await response.json();
        if (count) count.textContent = data.length;
        if (keywordText) keywordText.textContent = activeSearchLabel();

        if (!data.length) {
            if (grid) grid.innerHTML = "";
            if (empty) empty.classList.remove("hidden");
            return;
        }

        if (empty) empty.classList.add("hidden");
        if (grid) grid.innerHTML = data.map(renderProduct).join("");
    }

    function renderProduct(product) {
        const stockText = product.stockQuantity === null || product.stockQuantity === undefined
            ? "Chưa cập nhật kho"
            : product.stockQuantity > 0 ? `Còn ${product.stockQuantity}` : "Hết hàng";

        return `
            <article class="group overflow-hidden rounded-[2rem] border border-stone-200 bg-white shadow-sm transition hover:-translate-y-1 hover:shadow-xl">
                <div class="relative aspect-[4/5] overflow-hidden bg-stone-100">
                    <img src="${LongShop.imageUrl(product.imageUrl)}" alt="${LongShop.escapeHtml(product.productName)}" class="h-full w-full object-cover transition duration-500 group-hover:scale-105">
                    <button onclick="LongSearchPage.addFavorite(${product.productId})" class="absolute right-4 top-4 flex h-11 w-11 items-center justify-center rounded-full bg-white/95 text-lg text-red-500 shadow-lg transition hover:scale-105" aria-label="Yêu thích">♡</button>
                    <span class="absolute left-4 top-4 rounded-full bg-[#005b4b] px-3 py-1 text-xs font-bold text-white shadow">${LongShop.escapeHtml(product.concentration || "Parfumerie")}</span>
                    <button onclick="LongSearchPage.addToCart(${product.variantId || "null"})" class="absolute bottom-4 left-4 right-4 translate-y-4 rounded-full bg-stone-950 px-4 py-3 text-sm font-bold text-white opacity-0 shadow-xl transition group-hover:translate-y-0 group-hover:opacity-100 hover:bg-[#005b4b]">Thêm vào giỏ</button>
                </div>
                <div class="p-5">
                    <div class="flex items-center justify-between gap-3 text-xs uppercase tracking-[0.18em] text-stone-400">
                        <span>${LongShop.escapeHtml(product.brand || "Parfumerie")}</span>
                        <span>${LongShop.escapeHtml(product.gender || "Unisex")}</span>
                    </div>
                    <h3 class="mt-3 line-clamp-2 min-h-14 text-lg font-bold text-stone-950">${LongShop.escapeHtml(product.productName)}</h3>
                    <div class="mt-3 flex flex-wrap items-center gap-2 text-sm text-stone-500">
                        <span class="rounded-full bg-stone-100 px-3 py-1">${LongShop.escapeHtml(product.variantName || "Biến thể mặc định")}</span>
                        <span class="rounded-full bg-stone-100 px-3 py-1">${product.volumeMl || ""}ml</span>
                        <span class="rounded-full bg-stone-100 px-3 py-1">${stockText}</span>
                    </div>
                    <div class="mt-5 flex items-end justify-between gap-4">
                        <p class="text-xl font-extrabold text-red-600">${LongShop.formatCurrency(product.price)}</p>
                        <span class="text-xs text-stone-400">#${product.productId}</span>
                    </div>
                </div>
            </article>
        `;
    }

    function hydrateFromUrl() {
        const params = new URLSearchParams(window.location.search);
        const mapping = [
            ["q", "searchKeyword"],
            ["brand", "searchBrand"],
            ["gender", "searchGender"],
            ["concentration", "searchConcentration"],
            ["minPrice", "searchMinPrice"],
            ["maxPrice", "searchMaxPrice"],
            ["sort", "searchSort"]
        ];

        mapping.forEach(([key, id]) => {
            const element = document.getElementById(id);
            if (element && params.has(key)) element.value = params.get(key);
        });

        const type = params.get("type");
        const concentration = document.getElementById("searchConcentration");
        if (type && type.toLowerCase() === "chiet" && concentration && !concentration.value) {
            concentration.value = "Chiết";
        }
    }

    function resetFilters() {
        ["searchKeyword", "searchBrand", "searchGender", "searchConcentration", "searchMinPrice", "searchMaxPrice"].forEach(id => {
            const element = document.getElementById(id);
            if (element) element.value = "";
        });
        const sort = document.getElementById("searchSort");
        if (sort) sort.value = "latest";
        searchProducts(true);
    }

    async function addToCart(variantId) {
        await LongShop.addToCart(variantId, 1);
    }

    async function addFavorite(productId) {
        await LongShop.addFavorite(productId);
    }

    function bindEvents() {
        const form = document.getElementById("searchForm");
        if (form) {
            form.addEventListener("submit", event => {
                event.preventDefault();
                searchProducts(true);
            });
        }

        const resetButton = document.getElementById("searchResetButton");
        if (resetButton) resetButton.addEventListener("click", resetFilters);

        const sort = document.getElementById("searchSort");
        if (sort) sort.addEventListener("change", () => searchProducts(true));
    }

    document.addEventListener("DOMContentLoaded", () => {
        hydrateFromUrl();
        bindEvents();

        // Khi vừa mở /tim-kiem không có bộ lọc, sản phẩm đã được render sẵn từ server.
        // Chỉ gọi API khi URL có tham số lọc để tránh trang bị kẹt ở trạng thái "Đang tìm sản phẩm".
        if (window.location.search && window.location.search.length > 1) {
            searchProducts(false);
        }
    });

    return {searchProducts, resetFilters, addToCart, addFavorite};
})();
