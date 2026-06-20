(() => {
    const DEFAULT_EMAIL = "dang@test.com";
    const AUTH_KEYS = {
        accessToken: "parfumerie_access_token",
        email: "parfumerie_user_email"
    };

    function getEmail() {
        const authEmail = (window.ParfumerieAuth && typeof window.ParfumerieAuth.getUserEmail === "function")
            ? (window.ParfumerieAuth.getUserEmail() || "")
            : (localStorage.getItem(AUTH_KEYS.email) || "");
        const input = document.getElementById("longUserEmail");
        const inputEmail = input && input.value ? input.value.trim() : "";
        const savedEmail = localStorage.getItem("long-demo-email") || "";
        return authEmail.trim() || inputEmail || savedEmail || DEFAULT_EMAIL;
    }

    function getToken() {
        return (window.ParfumerieAuth && typeof window.ParfumerieAuth.getAccessToken === "function")
            ? (window.ParfumerieAuth.getAccessToken() || "")
            : (localStorage.getItem(AUTH_KEYS.accessToken) || "");
    }

    function setBadge(quantity) {
        const badge = document.getElementById("headerCartCount");
        if (!badge) return;

        const numberQuantity = Number(quantity || 0);
        badge.textContent = numberQuantity > 99 ? "99+" : String(numberQuantity);
    }

    async function refresh() {
        try {
            const requestHeaders = {"X-User-Email": getEmail()};
            // Không gửi Authorization ở request lấy badge giỏ hàng để tránh token cũ gây 401.
            const response = await fetch("/api/cart", {headers: requestHeaders});

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
