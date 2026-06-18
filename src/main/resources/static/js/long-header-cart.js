(() => {
    const DEFAULT_EMAIL = "dang@test.com";

    function getEmail() {
        const input = document.getElementById("longUserEmail");
        const inputEmail = input && input.value ? input.value.trim() : "";
        const savedEmail = localStorage.getItem("long-demo-email") || "";
        return inputEmail || savedEmail || DEFAULT_EMAIL;
    }

    function setBadge(quantity) {
        const badge = document.getElementById("headerCartCount");
        if (!badge) return;

        const numberQuantity = Number(quantity || 0);
        badge.textContent = numberQuantity > 99 ? "99+" : String(numberQuantity);
    }

    async function refresh() {
        try {
            const response = await fetch("/api/cart", {
                headers: {"X-User-Email": getEmail()}
            });

            if (!response.ok) {
                setBadge(0);
                return;
            }

            const cart = await response.json();
            setBadge(cart.totalQuantity || 0);
        } catch (error) {
            setBadge(0);
        }
    }

    window.LongHeaderCart = {refresh, setBadge};

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", refresh);
    } else {
        refresh();
    }
})();
