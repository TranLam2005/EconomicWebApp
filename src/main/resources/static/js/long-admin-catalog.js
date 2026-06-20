const LongAdminCatalog = (() => {
    let currentTab = "category";
    let categories = [];
    let brands = [];

    const $ = (id) => document.getElementById(id);

    function escapeHtml(value) {
        return String(value ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    function toast(message, type = "success") {
        const box = $("catalogToast");
        if (!box) return;
        box.className = `mb-6 rounded-2xl border px-5 py-4 text-sm font-bold ${type === "error" ? "border-red-200 bg-red-50 text-red-700" : "border-emerald-200 bg-emerald-50 text-emerald-700"}`;
        box.textContent = message;
        box.classList.remove("hidden");
        clearTimeout(box._timer);
        box._timer = setTimeout(() => box.classList.add("hidden"), 3500);
    }

    async function requestJson(url, options = {}) {
        const response = await fetch(url, {
            headers: {"Content-Type": "application/json", ...(options.headers || {})},
            ...options
        });

        if (response.status === 204) {
            return null;
        }

        const text = await response.text();
        const data = text ? JSON.parse(text) : null;
        if (!response.ok) {
            throw new Error(data?.message || `Lỗi ${response.status}`);
        }
        return data;
    }

    async function loadAll() {
        try {
            const [categoryData, brandData] = await Promise.all([
                requestJson("/api/long-admin/categories"),
                requestJson("/api/long-admin/brands")
            ]);
            categories = categoryData || [];
            brands = brandData || [];
            renderStats();
            renderTable();
        } catch (error) {
            toast(error.message || "Không thể tải dữ liệu", "error");
        }
    }

    function renderStats() {
        if ($("categoryCount")) $("categoryCount").textContent = categories.length;
        if ($("brandCount")) $("brandCount").textContent = brands.length;
    }

    function switchTab(tab) {
        currentTab = tab;
        $("catalogType").value = tab;
        resetForm(false);
        updateLabels();
        renderTable();
    }

    function updateLabels() {
        const isCategory = currentTab === "category";
        $("categoryTab").className = isCategory
            ? "rounded-[1.5rem] bg-[#004332] px-5 py-4 text-sm font-black uppercase tracking-[0.2em] text-white"
            : "rounded-[1.5rem] px-5 py-4 text-sm font-black uppercase tracking-[0.2em] text-stone-500 transition hover:bg-stone-50";
        $("brandTab").className = !isCategory
            ? "rounded-[1.5rem] bg-[#004332] px-5 py-4 text-sm font-black uppercase tracking-[0.2em] text-white"
            : "rounded-[1.5rem] px-5 py-4 text-sm font-black uppercase tracking-[0.2em] text-stone-500 transition hover:bg-stone-50";

        $("formTitle").textContent = isCategory ? "Thêm danh mục" : "Thêm thương hiệu";
        $("submitBtn").textContent = isCategory ? "Lưu danh mục" : "Lưu thương hiệu";
        $("tableKicker").textContent = isCategory ? "Danh sách danh mục" : "Danh sách thương hiệu";
        $("tableTitle").textContent = isCategory ? "Danh mục sản phẩm" : "Thương hiệu nước hoa";
        $("nameTypeLabel").textContent = isCategory ? "danh mục" : "thương hiệu";
        $("dynamicHead").textContent = isCategory ? "Mô tả" : "Trạng thái / Sản phẩm";
        $("brandActiveWrap").classList.toggle("hidden", isCategory);
        $("brandActiveWrap").classList.toggle("flex", !isCategory);
    }

    function currentItems() {
        const q = ($("catalogSearch")?.value || "").trim().toLowerCase();
        const source = currentTab === "category" ? categories : brands;
        if (!q) return source;
        return source.filter(item => [item.name, item.slug, item.description]
            .some(value => String(value || "").toLowerCase().includes(q)));
    }

    function renderTable() {
        const table = $("catalogTable");
        const empty = $("catalogEmpty");
        if (!table) return;

        const items = currentItems();
        table.innerHTML = items.map(item => currentTab === "category" ? categoryRow(item) : brandRow(item)).join("");
        empty.classList.toggle("hidden", items.length > 0);
    }

    function categoryRow(item) {
        return `
            <tr class="hover:bg-stone-50">
                <td class="px-5 py-4 align-top">
                    <div class="font-black text-stone-950">${escapeHtml(item.name)}</div>
                    <div class="mt-1 text-xs text-stone-400">ID: ${escapeHtml(item.id)}</div>
                </td>
                <td class="px-5 py-4 align-top"><code class="rounded-lg bg-stone-100 px-2 py-1 text-xs text-stone-700">${escapeHtml(item.slug || "-")}</code></td>
                <td class="max-w-sm px-5 py-4 align-top text-stone-600">${escapeHtml(item.description || "Không có mô tả")}</td>
                <td class="px-5 py-4 align-top text-right">
                    <button onclick="LongAdminCatalog.editItem('category', ${item.id})" class="rounded-full border border-stone-200 px-4 py-2 text-xs font-black text-stone-700 hover:border-[#004332] hover:text-[#004332]">Sửa</button>
                    <button onclick="LongAdminCatalog.deleteItem('category', ${item.id})" class="ml-2 rounded-full border border-red-200 px-4 py-2 text-xs font-black text-red-600 hover:bg-red-50">Xóa</button>
                </td>
            </tr>`;
    }

    function brandRow(item) {
        return `
            <tr class="hover:bg-stone-50">
                <td class="px-5 py-4 align-top">
                    <div class="font-black text-stone-950">${escapeHtml(item.name)}</div>
                    <div class="mt-1 text-xs text-stone-400">ID: ${escapeHtml(item.id)}</div>
                </td>
                <td class="px-5 py-4 align-top"><code class="rounded-lg bg-stone-100 px-2 py-1 text-xs text-stone-700">${escapeHtml(item.slug || "-")}</code></td>
                <td class="px-5 py-4 align-top text-stone-600">
                    <span class="inline-flex rounded-full ${item.active ? "bg-emerald-50 text-emerald-700" : "bg-stone-100 text-stone-500"} px-3 py-1 text-xs font-black">${item.active ? "Đang hiển thị" : "Đang ẩn"}</span>
                    <div class="mt-2 text-xs text-stone-400">${Number(item.productCount || 0)} sản phẩm đang dùng brand này</div>
                </td>
                <td class="px-5 py-4 align-top text-right">
                    <button onclick="LongAdminCatalog.editItem('brand', ${item.id})" class="rounded-full border border-stone-200 px-4 py-2 text-xs font-black text-stone-700 hover:border-[#004332] hover:text-[#004332]">Sửa</button>
                    <button onclick="LongAdminCatalog.deleteItem('brand', ${item.id})" class="ml-2 rounded-full border border-red-200 px-4 py-2 text-xs font-black text-red-600 hover:bg-red-50">Xóa</button>
                </td>
            </tr>`;
    }

    function resetForm(clearTab = true) {
        $("catalogId").value = "";
        $("catalogName").value = "";
        $("catalogSlug").value = "";
        $("catalogDescription").value = "";
        $("brandActive").checked = true;
        $("formMode").textContent = "Thêm mới";
        if (clearTab) updateLabels();
    }

    function editItem(type, id) {
        if (currentTab !== type) switchTab(type);
        const item = (type === "category" ? categories : brands).find(row => Number(row.id) === Number(id));
        if (!item) return;
        $("catalogId").value = item.id;
        $("catalogName").value = item.name || "";
        $("catalogSlug").value = item.slug || "";
        $("catalogDescription").value = item.description || "";
        $("brandActive").checked = item.active !== false;
        $("formMode").textContent = "Đang sửa";
        $("formTitle").textContent = type === "category" ? "Sửa danh mục" : "Sửa thương hiệu";
        $("submitBtn").textContent = type === "category" ? "Cập nhật danh mục" : "Cập nhật thương hiệu";
        window.scrollTo({top: 220, behavior: "smooth"});
    }

    async function saveForm(event) {
        event.preventDefault();
        const id = $("catalogId").value;
        const type = currentTab;
        const payload = {
            name: $("catalogName").value.trim(),
            slug: $("catalogSlug").value.trim(),
            description: $("catalogDescription").value.trim(),
            active: type === "brand" ? $("brandActive").checked : null
        };

        if (!payload.name) {
            toast("Vui lòng nhập tên", "error");
            return;
        }

        const url = type === "category" ? `/api/long-admin/categories${id ? `/${id}` : ""}` : `/api/long-admin/brands${id ? `/${id}` : ""}`;
        const method = id ? "PUT" : "POST";

        try {
            await requestJson(url, {method, body: JSON.stringify(payload)});
            toast(id ? "Cập nhật thành công" : "Thêm mới thành công");
            resetForm();
            await loadAll();
        } catch (error) {
            toast(error.message || "Lưu thất bại", "error");
        }
    }

    async function deleteItem(type, id) {
        const label = type === "category" ? "danh mục" : "thương hiệu";
        if (!confirm(`Bạn chắc chắn muốn xóa ${label} này?`)) return;
        const url = type === "category" ? `/api/long-admin/categories/${id}` : `/api/long-admin/brands/${id}`;
        try {
            await requestJson(url, {method: "DELETE"});
            toast("Đã xóa thành công");
            await loadAll();
        } catch (error) {
            toast(error.message || "Xóa thất bại", "error");
        }
    }

    function init() {
        $("catalogForm")?.addEventListener("submit", saveForm);
        $("catalogSearch")?.addEventListener("input", renderTable);
        updateLabels();
        loadAll();
    }

    document.addEventListener("DOMContentLoaded", init);

    return {switchTab, resetForm, editItem, deleteItem};
})();
