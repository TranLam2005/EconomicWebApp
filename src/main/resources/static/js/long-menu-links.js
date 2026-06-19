(() => {
    const CLOSE_DELAY_MS = 500;
    const LINK_CLASS = "block px-5 py-3 text-[16px] text-neutral-700 hover:bg-stone-50 hover:text-[#005b4b] relative z-[10000] cursor-pointer pointer-events-auto";
    const BRAND_LINK_CLASS = "block py-1.5 text-[16px] leading-6 text-black hover:text-[#005b4b] relative z-[10000] cursor-pointer pointer-events-auto";

    function escapeHtml(value) {
        return String(value ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    function normalize(value) {
        return String(value || "")
            .normalize("NFD")
            .replace(/[\u0300-\u036f]/g, "")
            .replace(/đ/g, "d")
            .replace(/Đ/g, "D")
            .toLowerCase();
    }

    function searchUrl(param, value) {
        const params = new URLSearchParams();
        params.set(param, value || "");
        return `/tim-kiem?${params.toString()}`;
    }

    function emptyMenu(message) {
        return `
            <div class="px-5 py-5 text-center normal-case tracking-normal text-stone-500">
                <p class="text-[15px]">${escapeHtml(message)}</p>
                <a href="/local-admin/catalog" class="mt-3 inline-flex rounded-full bg-[#005b4b] px-4 py-2 text-sm font-bold text-white hover:bg-stone-950">Thêm trong admin</a>
            </div>`;
    }

    function renderBrandMenu(brands) {
        const holder = document.getElementById("longBrandMenuGrid");
        if (!holder) return;
        const visibleBrands = (brands || []).filter(brand => brand && brand.name && brand.active !== false);
        if (!visibleBrands.length) {
            holder.innerHTML = `<div class="col-span-full">${emptyMenu("Chưa có thương hiệu. Hãy nhập thương hiệu bằng tay trong trang quản lý.")}</div>`;
            return;
        }

        holder.innerHTML = visibleBrands.map(brand => `
            <a href="${searchUrl("brand", brand.name)}" data-long-menu-link class="${BRAND_LINK_CLASS}">
                ${escapeHtml(brand.name)}
            </a>
        `).join("");
    }

    function renderCategoryMenu(categories) {
        const holder = document.getElementById("longCategoryMenuList");
        if (!holder) return;
        const items = (categories || []).filter(category => category && category.name);
        if (!items.length) {
            holder.innerHTML = emptyMenu("Chưa có danh mục. Hãy nhập danh mục bằng tay trong trang quản lý.");
            return;
        }

        holder.innerHTML = items.map(category => `
            <a href="${searchUrl("category", category.name)}" data-long-menu-link class="${LINK_CLASS}">
                ${escapeHtml(category.name)}
            </a>
        `).join("");
    }

    function renderExtractMenu(categories) {
        const holder = document.getElementById("longExtractMenuList");
        if (!holder) return;
        const items = (categories || []).filter(category => category && category.name && normalize(category.name).includes("chiet"));
        if (!items.length) {
            holder.innerHTML = emptyMenu("Chưa có danh mục nước hoa chiết. Nhập danh mục có chữ “chiết” để hiện ở đây.");
            return;
        }

        holder.innerHTML = items.map(category => `
            <a href="${searchUrl("category", category.name)}" data-long-menu-link class="${LINK_CLASS}">
                ${escapeHtml(category.name)}
            </a>
        `).join("");
    }

    async function loadCatalogMenu() {
        try {
            const response = await fetch("/api/long/catalog/menu", {headers: {"Accept": "application/json"}});
            if (!response.ok) throw new Error(`Lỗi ${response.status}`);
            const data = await response.json();
            renderBrandMenu(data.brands || []);
            renderCategoryMenu(data.categories || []);
            renderExtractMenu(data.categories || []);
        } catch (error) {
            renderBrandMenu([]);
            renderCategoryMenu([]);
            renderExtractMenu([]);
            console.warn("Không thể tải menu danh mục/thương hiệu", error);
        }
    }

    function getPanel(menuItem) {
        if (!menuItem) return null;
        return Array.from(menuItem.children).find(child => {
            const className = child.getAttribute && child.getAttribute("class");
            return className && className.includes("absolute");
        });
    }

    function showPanel(menuItem, panel) {
        if (!menuItem || !panel) return;
        clearTimeout(menuItem.__longMenuTimer);
        menuItem.classList.add("long-menu-open");
        panel.style.visibility = "visible";
        panel.style.opacity = "1";
        panel.style.pointerEvents = "auto";
        panel.style.transform = "translateY(0)";
    }

    function hidePanel(menuItem, panel) {
        if (!menuItem || !panel) return;
        clearTimeout(menuItem.__longMenuTimer);
        menuItem.__longMenuTimer = setTimeout(() => {
            if (menuItem.matches(":hover") || panel.matches(":hover") || menuItem.contains(document.activeElement)) {
                showPanel(menuItem, panel);
                return;
            }
            menuItem.classList.remove("long-menu-open");
            panel.style.visibility = "";
            panel.style.opacity = "";
            panel.style.pointerEvents = "";
            panel.style.transform = "";
        }, CLOSE_DELAY_MS);
    }

    function closeOtherMenus(currentMenuItem) {
        document.querySelectorAll("[data-long-menu-trigger]").forEach(trigger => {
            const menuItem = trigger.parentElement;
            const panel = getPanel(menuItem);
            if (!menuItem || !panel || menuItem === currentMenuItem) return;
            menuItem.classList.remove("long-menu-open");
            panel.style.visibility = "";
            panel.style.opacity = "";
            panel.style.pointerEvents = "";
            panel.style.transform = "";
        });
    }

    function goToLink(event, link) {
        const href = link.getAttribute("href");
        if (!href || href === "#") return;
        event.preventDefault();
        event.stopPropagation();
        window.location.assign(href);
    }

    function bindMenu(trigger) {
        if (trigger.__longMenuBound) return;
        trigger.__longMenuBound = true;

        const menuItem = trigger.parentElement;
        const panel = getPanel(menuItem);
        if (!menuItem || !panel) return;

        panel.style.pointerEvents = "auto";

        const open = () => {
            clearTimeout(menuItem.__longMenuTimer);
            closeOtherMenus(menuItem);
            showPanel(menuItem, panel);
        };
        const scheduleClose = () => hidePanel(menuItem, panel);

        trigger.addEventListener("mouseenter", open);
        menuItem.addEventListener("mouseenter", open);
        panel.addEventListener("mouseenter", open);

        trigger.addEventListener("mouseleave", scheduleClose);
        menuItem.addEventListener("mouseleave", scheduleClose);
        panel.addEventListener("mouseleave", scheduleClose);

        trigger.addEventListener("focus", open);
        panel.addEventListener("focusin", open);
        panel.addEventListener("focusout", scheduleClose);
        trigger.addEventListener("click", event => goToLink(event, trigger));
    }

    function bindMenuLinks() {
        document.querySelectorAll("a[data-long-menu-link]").forEach(link => {
            if (link.__longMenuLinkBound) return;
            link.__longMenuLinkBound = true;
            link.addEventListener("click", event => goToLink(event, link), true);
            link.addEventListener("mousedown", event => event.stopPropagation(), true);
        });
    }

    function bindAllMenus() {
        document.querySelectorAll("[data-long-menu-trigger]").forEach(bindMenu);
        bindMenuLinks();
    }

    document.addEventListener("DOMContentLoaded", async () => {
        await loadCatalogMenu();
        bindAllMenus();

        document.addEventListener("keydown", event => {
            if (event.key !== "Escape") return;
            document.querySelectorAll("[data-long-menu-trigger]").forEach(trigger => {
                const menuItem = trigger.parentElement;
                const panel = getPanel(menuItem);
                if (!menuItem || !panel) return;
                menuItem.classList.remove("long-menu-open");
                panel.style.visibility = "";
                panel.style.opacity = "";
                panel.style.pointerEvents = "";
                panel.style.transform = "";
            });
        });
    });
})();
