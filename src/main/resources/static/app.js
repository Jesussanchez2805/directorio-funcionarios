const API_URL = "/api/v1/funcionarios";
const AUTH_STORAGE_KEY = "directorio.auth";

if (!sessionStorage.getItem(AUTH_STORAGE_KEY)) {
    window.location.replace("/login.html");
}

const employeeForm = document.querySelector("#employee-form");
const rows = document.querySelector("#employee-rows");
const emptyState = document.querySelector("#empty-state");
const notice = document.querySelector("#notice");
const formTitle = document.querySelector("#form-title");
const formMode = document.querySelector("#form-mode");
const saveButton = document.querySelector("#save-button");

function showNotice(message, type = "info") {
    notice.textContent = message;
    notice.className = `notice ${type}`;
    notice.hidden = false;
}

function clearNotice() {
    notice.hidden = true;
    notice.textContent = "";
}

async function request(url, options = {}) {
    const response = await fetch(url, {
        ...options,
        headers: {
            Authorization: sessionStorage.getItem(AUTH_STORAGE_KEY),
            ...(options.body ? { "Content-Type": "application/json" } : {}),
            ...options.headers
        }
    });

    if (response.status === 401) {
        sessionStorage.removeItem(AUTH_STORAGE_KEY);
        window.location.replace("/login.html");
        throw new Error("La sesión expiró. Inicia sesión nuevamente.");
    }

    if (!response.ok) {
        let message = `La solicitud falló (${response.status}).`;
        try {
            const body = await response.json();
            message = body.error || body.detail || message;
        } catch {
            // Conserva el mensaje HTTP cuando la respuesta no incluye JSON.
        }
        throw new Error(message);
    }

    return response.status === 204 ? null : response.json();
}

function setRows(employees) {
    rows.replaceChildren();
    emptyState.hidden = employees.length > 0;

    for (const employee of employees) {
        const row = document.createElement("tr");
        const values = [
            employee.id,
            employee.nombre,
            employee.cargo,
            employee.departamento,
            employee.telefono || "—"
        ];

        for (const [index, value] of values.entries()) {
            const cell = document.createElement("td");
            cell.textContent = value;
            if (index === 1) cell.className = "employee-name";
            row.append(cell);
        }

        const actionsCell = document.createElement("td");
        const actions = document.createElement("div");
        actions.className = "row-actions";
        actions.append(
            createActionButton("Editar", () => editEmployee(employee)),
            createActionButton("Eliminar", () => deleteEmployee(employee), true)
        );
        actionsCell.append(actions);
        row.append(actionsCell);
        rows.append(row);
    }
}

function createActionButton(label, action, isDelete = false) {
    const button = document.createElement("button");
    button.type = "button";
    button.className = `action-button${isDelete ? " delete" : ""}`;
    button.textContent = label;
    button.addEventListener("click", action);
    return button;
}

async function loadEmployees(query = "") {
    try {
        const result = await request(`${API_URL}${query}`);
        const employees = result || [];
        setRows(employees);
        if (!employees.length) showNotice("No se encontraron funcionarios.", "info");
        else clearNotice();
    } catch (error) {
        setRows([]);
        showNotice(error.message, "error");
    }
}

function editEmployee(employee) {
    document.querySelector("#employee-id").value = employee.id;
    document.querySelector("#nombre").value = employee.nombre;
    document.querySelector("#cargo").value = employee.cargo;
    document.querySelector("#departamento").value = employee.departamento;
    document.querySelector("#telefono").value = employee.telefono || "";
    formTitle.textContent = "Editar funcionario";
    formMode.textContent = `ID ${employee.id}`;
    saveButton.textContent = "Guardar cambios";
    document.querySelector("#nombre").focus();
    window.scrollTo({ top: 0, behavior: "smooth" });
}

employeeForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    const id = document.querySelector("#employee-id").value;
    const payload = {
        nombre: document.querySelector("#nombre").value.trim(),
        cargo: document.querySelector("#cargo").value.trim(),
        departamento: document.querySelector("#departamento").value.trim(),
        telefono: document.querySelector("#telefono").value.trim() || null
    };

    try {
        const employee = await request(id ? `${API_URL}/${id}` : API_URL, {
            method: id ? "PUT" : "POST",
            body: JSON.stringify(payload)
        });
        employeeForm.reset();
        formTitle.textContent = "Nuevo funcionario";
        formMode.textContent = "Nuevo";
        saveButton.textContent = "Guardar funcionario";
        await loadEmployees();
        showNotice(id ? "Funcionario actualizado correctamente." : `Funcionario registrado con ID ${employee.id}.`, "success");
    } catch (error) {
        showNotice(error.message, "error");
    }
});

employeeForm.addEventListener("reset", () => {
    window.setTimeout(() => {
        formTitle.textContent = "Nuevo funcionario";
        formMode.textContent = "Nuevo";
        saveButton.textContent = "Guardar funcionario";
    });
});

document.querySelector("#id-search-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const id = document.querySelector("#id-search").value;
    try {
        const employee = await request(`${API_URL}/${encodeURIComponent(id)}`);
        setRows([employee]);
        clearNotice();
    } catch (error) {
        setRows([]);
        showNotice(error.message, "error");
    }
});

document.querySelector("#filter-form").addEventListener("submit", (event) => {
    event.preventDefault();
    const params = new URLSearchParams();
    const name = document.querySelector("#name-filter").value.trim();
    const department = document.querySelector("#department-filter").value.trim();
    if (name) params.set("nombre", name);
    if (department) params.set("departamento", department);
    const query = params.toString();
    loadEmployees(query ? `?${query}` : "");
});

document.querySelector("#show-all").addEventListener("click", () => {
    document.querySelector("#id-search-form").reset();
    document.querySelector("#filter-form").reset();
    loadEmployees();
});

document.querySelector("#logout-button").addEventListener("click", () => {
    sessionStorage.removeItem(AUTH_STORAGE_KEY);
    window.location.replace("/login.html");
});

async function deleteEmployee(employee) {
    if (!window.confirm(`¿Eliminar a ${employee.nombre} del directorio?`)) return;
    try {
        await request(`${API_URL}/${employee.id}`, { method: "DELETE" });
        await loadEmployees();
        showNotice("Funcionario eliminado correctamente.", "success");
    } catch (error) {
        showNotice(error.message, "error");
    }
}

loadEmployees();
