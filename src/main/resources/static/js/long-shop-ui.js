const LongShop = (() => {
    const DEFAULT_EMAIL = "dang@test.com";
    const FALLBACK_IMAGE = "https://images.unsplash.com/photo-1592945403244-b3fbafd7f539?auto=format&fit=crop&w=800&q=80";

    function formatCurrency(value) {
        const numberValue = Number(value || 0);
        return numberValue.toLocaleString("vi-VN", {
            style: "currency",
            currency: "VND",
            maximumFractionDigits: 0
        });
    }

    function getEmail() {
        const emailInput = document.getElementById("longUserEmail");
        const email = emailInput && emailInput.value ? emailInput.value.trim() : "";
        const savedEmail = localStorage.getItem("long-demo-email") || "";
        const finalEmail = email || savedEmail || DEFAULT_EMAIL;

        if (emailInput && emailInput.value !== finalEmail) {
            emailInput.value = finalEmail;
        }

        localStorage.setItem("long-demo-email", finalEmail);
        return finalEmail;
    }

    function setEmailFromInput() {
        const emailInput = document.getElementById("longUserEmail");
        if (emailInput) {
            emailInput.addEventListener("change", () => getEmail());
            emailInput.addEventListener("blur", () => getEmail());
            getEmail();
        }
    }

    function headers(json = false) {
        const baseHeaders = {"X-User-Email": getEmail()};
        if (json) {
            baseHeaders["Content-Type"] = "application/json";
        }
        return baseHeaders;
    }

    function escapeHtml(value) {
        return String(value ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    function imageUrl(url) {
        return url && String(url).trim() ? url : FALLBACK_IMAGE;
    }

    function toast(message, type = "success") {
        let toastBox = document.getElementById("longToast");
        if (!toastBox) {
            toastBox = document.createElement("div");
            toastBox.id = "longToast";
            toastBox.className = "fixed bottom-6 right-6 z-50 max-w-sm rounded-2xl px-5 py-4 text-sm font-semibold text-white shadow-2xl transition";
            document.body.appendChild(toastBox);
        }

        const bgClass = type === "error" ? "bg-red-600" : type === "warning" ? "bg-amber-600" : "bg-[#005b4b]";
        toastBox.className = `fixed bottom-6 right-6 z-50 max-w-sm rounded-2xl px-5 py-4 text-sm font-semibold text-white shadow-2xl transition ${bgClass}`;
        toastBox.textContent = message;
        toastBox.style.opacity = "1";
        toastBox.style.transform = "translateY(0)";

        window.clearTimeout(window.__longToastTimer);
        window.__longToastTimer = window.setTimeout(() => {
            toastBox.style.opacity = "0";
            toastBox.style.transform = "translateY(12px)";
        }, 2600);
    }

    async function addToCart(variantId, quantity = 1) {
        if (!variantId) {
            toast("Sản phẩm này chưa có biến thể để thêm vào giỏ.", "warning");
            return false;
        }

        const response = await fetch("/api/cart", {
            method: "POST",
            headers: headers(true),
            body: JSON.stringify({variantId: Number(variantId), quantity: Number(quantity || 1)})
        });

        if (!response.ok) {
            const text = await response.text();
            toast(`Thêm giỏ hàng thất bại: ${response.status} - ${text}`, "error");
            return false;
        }

        toast("Đã thêm sản phẩm vào giỏ hàng.");
        if (window.LongHeaderCart && typeof window.LongHeaderCart.refresh === "function") {
            window.LongHeaderCart.refresh();
        }
        return true;
    }

    async function addFavorite(productId) {
        if (!productId) {
            toast("Không tìm thấy mã sản phẩm.", "warning");
            return false;
        }

        const response = await fetch(`/api/favorites/${productId}`, {
            method: "POST",
            headers: headers(false)
        });

        if (!response.ok) {
            const text = await response.text();
            toast(`Thêm yêu thích thất bại: ${response.status} - ${text}`, "error");
            return false;
        }

        toast("Đã thêm vào danh sách yêu thích.");
        return true;
    }

    async function removeFavorite(productId) {
        if (!productId) {
            toast("Không tìm thấy mã sản phẩm.", "warning");
            return false;
        }

        const response = await fetch(`/api/favorites/${productId}`, {
            method: "DELETE",
            headers: headers(false)
        });

        if (!response.ok) {
            const text = await response.text();
            toast(`Xóa yêu thích thất bại: ${response.status} - ${text}`, "error");
            return false;
        }

        toast("Đã xóa khỏi danh sách yêu thích.");
        return true;
    }

    async function getFirstVariantId(productId) {
        if (!productId) {
            return null;
        }

        const response = await fetch(`/api/long/products?productId=${productId}&limit=1`, {
            headers: headers(false)
        });

        if (!response.ok) {
            return null;
        }

        const data = await response.json();
        return data && data.length ? data[0].variantId : null;
    }

    document.addEventListener("DOMContentLoaded", setEmailFromInput);

    return {
        DEFAULT_EMAIL,
        FALLBACK_IMAGE,
        formatCurrency,
        getEmail,
        headers,
        escapeHtml,
        imageUrl,
        toast,
        addToCart,
        addFavorite,
        removeFavorite,
        getFirstVariantId
    };
})();
