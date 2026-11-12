const AUTH_STORAGE_KEY = "directorio.auth";
const loginForm = document.querySelector("#login-form");
const loginError = document.querySelector("#login-error");
const submitButton = document.querySelector("#submit-button");
let lockoutTimer;

if (sessionStorage.getItem(AUTH_STORAGE_KEY)) {
    window.location.replace("/index.html");
}

function encodeBasicCredentials(username, password) {
    const bytes = new TextEncoder().encode(`${username}:${password}`);
    let binary = "";
    for (const byte of bytes) binary += String.fromCharCode(byte);
    return `Basic ${btoa(binary)}`;
}

function startLockoutCountdown(seconds) {
    let remaining = seconds;
    submitButton.disabled = true;
    submitButton.textContent = `Espera ${remaining} s`;

    window.clearInterval(lockoutTimer);
    lockoutTimer = window.setInterval(() => {
        remaining -= 1;
        if (remaining <= 0) {
            window.clearInterval(lockoutTimer);
            submitButton.disabled = false;
            submitButton.textContent = "Iniciar sesión";
            loginError.textContent = "Ya puedes volver a intentarlo.";
            return;
        }
        submitButton.textContent = `Espera ${remaining} s`;
    }, 1000);
}

loginForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    loginError.hidden = true;
    submitButton.disabled = true;
    submitButton.textContent = "Validando...";

    const username = document.querySelector("#username").value.trim();
    const password = document.querySelector("#password").value;
    const authorization = encodeBasicCredentials(username, password);

    try {
        const response = await fetch("/api/v1/auth/validar", {
            method: "POST",
            headers: { Authorization: authorization }
        });

        if (!response.ok) {
            const body = await response.json().catch(() => ({}));
            const error = new Error(body.error || `No fue posible validar las credenciales (${response.status}).`);
            error.status = response.status;
            error.retryAfter = Number(response.headers.get("Retry-After")) || Number(body.segundosEspera) || 0;
            throw error;
        }

        sessionStorage.setItem(AUTH_STORAGE_KEY, authorization);
        window.location.assign("/index.html");
    } catch (error) {
        loginError.textContent = error instanceof TypeError
            ? "No se pudo conectar con el servidor. Intenta nuevamente."
            : error.message;
        loginError.hidden = false;
        if (error.status === 429 && error.retryAfter > 0) {
            startLockoutCountdown(error.retryAfter);
        } else {
            submitButton.disabled = false;
            submitButton.textContent = "Iniciar sesión";
        }
    }
});
