(() => {
    const OPEN_DELAY_MS = 0;
    const CLOSE_DELAY_MS = 550;

    function getPanel(menuItem) {
        if (!menuItem) return null;
        return Array.from(menuItem.children).find(child => {
            const className = child.getAttribute && child.getAttribute("class");
            return className && className.includes("absolute") && child.querySelector("a[data-long-menu-link]");
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
        const menuItem = trigger.parentElement;
        const panel = getPanel(menuItem);
        if (!menuItem || !panel) return;

        menuItem.style.position = menuItem.classList.contains("relative") ? "" : menuItem.style.position;
        panel.style.pointerEvents = "auto";

        const open = () => {
            clearTimeout(menuItem.__longMenuTimer);
            closeOtherMenus(menuItem);
            setTimeout(() => showPanel(menuItem, panel), OPEN_DELAY_MS);
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

        // Bấm vào tiêu đề menu sẽ đi tới trang tìm kiếm tổng, còn hover vẫn mở menu con.
        trigger.addEventListener("click", event => goToLink(event, trigger));
    }

    document.addEventListener("DOMContentLoaded", () => {
        document.querySelectorAll("[data-long-menu-trigger]").forEach(bindMenu);

        document.querySelectorAll("a[data-long-menu-link]").forEach(link => {
            link.addEventListener("click", event => goToLink(event, link), true);
            link.addEventListener("mousedown", event => event.stopPropagation(), true);
        });

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
