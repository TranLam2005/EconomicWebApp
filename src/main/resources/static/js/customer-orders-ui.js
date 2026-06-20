(function () {
    const FALLBACK_IMAGE = "https://images.unsplash.com/photo-1592945403244-b3fbafd7f539?auto=format&fit=crop&w=800&q=80";

    function $(id) {
        return document.getElementById(id);
    }

    function getEmail() {
        if (window.ParfumerieAuth && typeof window.ParfumerieAuth.getUserEmail === "function") {
            return (window.ParfumerieAuth.getUserEmail() || "").trim();
        }
        return (localStorage.getItem("parfumerie_user_email") || "").trim();
    }

    function formatCurrency(value) {
        return Number(value || 0).toLocaleString("vi-VN", {
            style: "currency",
            currency: "VND",
            maximumFractionDigits: 0
        });
    }

    function formatDate(value) {
        if (!value) return "";
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) return value;
        return date.toLocaleString("vi-VN", {
            day: "2-digit",
            month: "2-digit",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit"
        });
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

    function setVisible(id, visible) {
        const element = $(id);
        if (element) element.classList.toggle("hidden", !visible);
    }

    function showMessage(message, type) {
        const box = $("ordersError");
        if (!box) return;
        box.textContent = message;
        box.className = "mt-6 rounded px-5 py-4 text-sm font-semibold";
        if (type === "success") {
            box.classList.add("border", "border-emerald-200", "bg-emerald-50", "text-emerald-700");
        } else {
            box.classList.add("border", "border-red-200", "bg-red-50", "text-red-700");
        }
        setVisible("ordersError", true);
    }

    function paymentText(method) {
        if (method === "COD") return "Thanh toán khi giao hàng (COD)";
        if (method === "VNPAY") return "Chuyển khoản/VNPAY";
        if (method === "BANK_TRANSFER") return "Chuyển khoản qua ngân hàng";
        return method || "Chưa cập nhật";
    }

    function cancelButton(order) {
        if (!order.cancelable || order.status !== "PENDING") return "";
        return `
            <button type="button"
                    class="cancel-order-btn inline-flex h-10 items-center justify-center rounded border border-red-200 bg-white px-4 text-sm font-bold text-red-600 hover:bg-red-50"
                    data-order-id="${Number(order.id || 0)}"
                    data-order-code="${escapeHtml(order.orderCode || ("DH" + order.id))}">
                Hủy đơn hàng
            </button>
        `;
    }

    function renderOrders(orders) {
        const list = $("ordersList");
        if (!list) return;

        if (!orders.length) {
            list.innerHTML = "";
            setVisible("ordersEmpty", true);
            return;
        }

        setVisible("ordersEmpty", false);
        list.innerHTML = orders.map(order => {
            const items = order.items || [];
            const status = order.status || "PENDING";
            const code = order.orderCode || ("DH" + order.id);
            return `
                <article class="overflow-hidden rounded border border-neutral-200 bg-white">
                    <div class="flex flex-col gap-3 bg-[#fafafa] px-5 py-4 md:flex-row md:items-center md:justify-between">
                        <div>
                            <div class="flex flex-wrap items-center gap-2">
                                <h2 class="text-lg font-black text-neutral-950">Đơn hàng #${escapeHtml(code).slice(0, 12)}</h2>
                                <span class="status-pill status-${escapeHtml(status)} rounded-full px-3 py-1 text-xs font-bold">${escapeHtml(order.statusText || status)}</span>
                            </div>
                            <p class="mt-1 text-sm text-neutral-500">Ngày đặt: ${escapeHtml(formatDate(order.createdAt))}</p>
                        </div>
                        <div class="flex flex-col gap-3 text-left md:items-end md:text-right">
                            <div>
                                <p class="text-sm text-neutral-500">Tổng cộng</p>
                                <p class="text-xl font-black text-[#005b4b]">${formatCurrency(order.totalAmount)}</p>
                            </div>
                            ${cancelButton(order)}
                        </div>
                    </div>

                    <div class="grid gap-5 p-5 lg:grid-cols-[1fr_320px]">
                        <div class="space-y-3">
                            ${items.map(item => `
                                <div class="flex gap-3 rounded border border-neutral-100 bg-white p-3">
                                    <div class="h-16 w-16 shrink-0 overflow-hidden rounded border border-neutral-200 bg-neutral-50">
                                        <img src="${imageUrl(item.imageUrl)}" alt="${escapeHtml(item.productName)}" class="h-full w-full object-cover">
                                    </div>
                                    <div class="min-w-0 flex-1">
                                        <h3 class="truncate font-bold text-neutral-900">${escapeHtml(item.productName)}</h3>
                                        <p class="mt-1 text-xs text-neutral-500">${escapeHtml(item.brand || "Parfumerie")}</p>
                                        <p class="mt-1 text-xs text-neutral-500">Số lượng: <b>${Number(item.quantity || 0)}</b></p>
                                    </div>
                                    <div class="shrink-0 text-right text-sm font-bold text-neutral-700">
                                        ${formatCurrency(item.lineTotal)}
                                    </div>
                                </div>
                            `).join("")}
                        </div>

                        <div class="rounded border border-neutral-200 bg-[#fafafa] p-4 text-sm">
                            <h3 class="font-black text-neutral-950">Thông tin giao hàng</h3>
                            <div class="mt-3 space-y-2 text-neutral-600">
                                <p><b>Người nhận:</b> ${escapeHtml(order.customerName || "")}</p>
                                <p><b>Email:</b> ${escapeHtml(order.customerEmail || "")}</p>
                                <p><b>SĐT:</b> ${escapeHtml(order.customerPhone || "")}</p>
                                <p><b>Địa chỉ:</b> ${escapeHtml(order.shippingAddress || "")}</p>
                                <p><b>Thanh toán:</b> ${escapeHtml(paymentText(order.paymentMethod))}</p>
                            </div>
                            <div class="mt-4 border-t border-neutral-200 pt-4 space-y-2">
                                <div class="flex justify-between"><span>Tạm tính</span><b>${formatCurrency(order.subtotalAmount)}</b></div>
                                <div class="flex justify-between"><span>Phí vận chuyển</span><b>${formatCurrency(order.shippingFee)}</b></div>
                                <div class="flex justify-between"><span>Giảm giá</span><b>${formatCurrency(order.discountAmount)}</b></div>
                                <div class="flex justify-between border-t border-neutral-200 pt-3 text-base"><span class="font-bold">Tổng cộng</span><b class="text-[#005b4b]">${formatCurrency(order.totalAmount)}</b></div>
                            </div>
                        </div>
                    </div>
                </article>
            `;
        }).join("");

        document.querySelectorAll(".cancel-order-btn").forEach(button => {
            button.addEventListener("click", function () {
                const orderId = this.dataset.orderId;
                const orderCode = this.dataset.orderCode || orderId;
                cancelOrder(orderId, orderCode, this);
            });
        });
    }

    async function cancelOrder(orderId, orderCode, button) {
        if (!orderId) return;
        const ok = window.confirm(`Bạn chắc chắn muốn hủy đơn hàng ${orderCode}?`);
        if (!ok) return;

        const email = getEmail();
        if (!email) {
            showMessage("Bạn cần đăng nhập để hủy đơn hàng.", "error");
            return;
        }

        const oldText = button ? button.textContent : "";
        if (button) {
            button.disabled = true;
            button.textContent = "Đang hủy...";
        }

        try {
            const response = await fetch(`/public/api/orders/${encodeURIComponent(orderId)}/cancel`, {
                method: "POST",
                headers: { "X-User-Email": email }
            });
            if (!response.ok) {
                let message = "Không hủy được đơn hàng.";
                try {
                    const data = await response.json();
                    if (data && data.message) message = data.message;
                } catch (_) {
                    const text = await response.text().catch(() => "");
                    if (text) message = text;
                }
                throw new Error(message);
            }
            showMessage("Đã hủy đơn hàng thành công.", "success");
            await loadOrders();
        } catch (error) {
            showMessage(error && error.message ? error.message : "Không hủy được đơn hàng.", "error");
        } finally {
            if (button) {
                button.disabled = false;
                button.textContent = oldText;
            }
        }
    }

    async function loadOrders() {
        const email = getEmail();
        setVisible("ordersError", false);
        setVisible("ordersEmpty", false);
        setVisible("ordersLoading", true);

        if (!email) {
            setVisible("ordersLoading", false);
            setVisible("ordersLoginBox", true);
            return;
        }

        setVisible("ordersLoginBox", false);
        try {
            const response = await fetch("/public/api/orders/my", {
                headers: { "X-User-Email": email }
            });
            if (!response.ok) {
                const text = await response.text().catch(() => "");
                throw new Error(text || "Không tải được đơn hàng.");
            }
            const orders = await response.json();
            renderOrders(Array.isArray(orders) ? orders : []);
        } catch (error) {
            showMessage("Không tải được đơn hàng: " + (error && error.message ? error.message : "Vui lòng thử lại."), "error");
        } finally {
            setVisible("ordersLoading", false);
        }
    }

    document.addEventListener("DOMContentLoaded", function () {
        const params = new URLSearchParams(window.location.search);
        if (params.get("success") === "1") {
            setVisible("orderSuccessMessage", true);
        }
        const reload = $("reloadOrdersButton");
        if (reload) reload.addEventListener("click", loadOrders);
        loadOrders();
    });
})();
