const AUTH_STORAGE_KEY = "directorio.auth";
const loginForm = document.querySelector("#login-form");
const loginError = document.querySelector("#login-error");
const submitButton = document.querySelector("#submit-button");

if (sessionStorage.getItem(AUTH_STORAGE_KEY)) {
    window.location.replace("/index.html");
}

function encodeBasicCredentials(username, password) {
    const bytes = new TextEncoder().encode(`${username}:${password}`);
    let binary = "";
    for (const byte of bytes) binary += String.fromCharCode(byte);
    return `Basic ${btoa(binary)}`;
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

        if (response.status === 401) {
            throw new Error("Usuario o contraseña incorrectos.");
        }
        if (!response.ok) {
            throw new Error(`No fue posible validar las credenciales (${response.status}).`);
        }

        sessionStorage.setItem(AUTH_STORAGE_KEY, authorization);
        window.location.assign("/index.html");
    } catch (error) {
        loginError.textContent = error instanceof TypeError
            ? "No se pudo conectar con el servidor. Intenta nuevamente."
            : error.message;
        loginError.hidden = false;
        submitButton.disabled = false;
        submitButton.textContent = "Iniciar sesión";
    }
});
