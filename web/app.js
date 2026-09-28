const loginView = document.querySelector('#loginView');
const dashboardView = document.querySelector('#dashboardView');
const loginForm = document.querySelector('#loginForm');
const loginError = document.querySelector('#loginError');
const dashboardContent = document.querySelector('#dashboardContent');
let account = null;

loginForm.addEventListener('submit', async (event) => {
  event.preventDefault();
  loginError.classList.add('d-none');
  const body = new URLSearchParams({
    username: document.querySelector('#username').value,
    password: document.querySelector('#password').value
  });
  const response = await fetch('/api/login', { method: 'POST', body });
  const data = await response.json();
  if (!response.ok) {
    loginError.textContent = data.error || 'Login failed';
    loginError.classList.remove('d-none');
    return;
  }
  account = data;
  loginView.classList.add('d-none');
  dashboardView.classList.remove('d-none');
  document.querySelector('#welcome').textContent = `Welcome, ${account.username}`;
  document.querySelector('#roleLabel').textContent = `${account.role} workspace`;
  await loadDashboard();
});

document.querySelector('#logoutButton').addEventListener('click', () => {
  account = null;
  dashboardView.classList.add('d-none');
  loginView.classList.remove('d-none');
  loginForm.reset();
});

async function loadDashboard() {
  const response = await fetch(`/api/dashboard?token=${encodeURIComponent(account.token)}`);
  const data = await response.json();
  if (account.role === 'ADMIN') renderAdmin(data);
  if (account.role === 'LECTURER') renderLecturer(data);
  if (account.role === 'STUDENT') renderStudent(data);
}

function renderAdmin(data) {
  dashboardContent.innerHTML = [
    metric('Students', data.students[0]?.total || 0),
    metric('Courses', data.courses[0]?.total || 0),
    metric('Published results', data.results[0]?.total || 0)
  ].join('');
}

function renderLecturer(data) {
  dashboardContent.innerHTML = `<div class="col-12"><div class="data-panel"><h2>Assigned courses</h2>${table(data.courses, ['courseCode', 'courseName', 'creditUnits'])}</div></div>`;
}

function renderStudent(data) {
  const profile = data.profile[0] || {};
  dashboardContent.innerHTML = `<div class="col-12"><div class="data-panel"><h2>${escapeHtml(profile.name || 'Student profile')}</h2><p class="muted">${escapeHtml(profile.program || '')} · ${escapeHtml(profile.department || '')}</p></div></div>
    <div class="col-md-4"><div class="data-panel"><h2>Registered courses</h2>${table(data.courses, ['courseCode', 'courseName'])}</div></div>
    <div class="col-md-4"><div class="data-panel"><h2>Results</h2>${table(data.results, ['courseCode', 'totalMark', 'grade'])}</div></div>
    <div class="col-md-4"><div class="data-panel"><h2>Fees</h2>${table(data.fees, ['academicYear', 'semesterName', 'amount', 'status'])}</div></div>`;
}

function metric(label, value) { return `<div class="col-md-4"><div class="metric"><span class="muted">${label}</span><strong>${escapeHtml(String(value))}</strong></div></div>`; }
function table(rows, columns) {
  if (!rows.length) return '<p class="muted mb-0">No records available.</p>';
  return `<div class="table-responsive"><table class="table mt-3"><thead><tr>${columns.map(column => `<th>${column}</th>`).join('')}</tr></thead><tbody>${rows.map(row => `<tr>${columns.map(column => `<td>${escapeHtml(String(row[column] ?? ''))}</td>`).join('')}</tr>`).join('')}</tbody></table></div>`;
}
function escapeHtml(value) { return value.replace(/[&<>'"]/g, character => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[character])); }
