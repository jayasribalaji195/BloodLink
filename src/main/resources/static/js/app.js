// BloodLink Frontend JavaScript Application
const API_BASE = '/api';

// Cache for blood groups and donors
let bloodGroupsList = [];
let allDonorsList = [];

// Initialize on DOM loaded
document.addEventListener('DOMContentLoaded', () => {
  initTabs();
  initDatePickers();
  loadBloodGroups();
  loadStats();
  loadAllDonors();
  loadRecentDonations();
  loadBloodGroupMatrix();

  // Attach Form Event Listeners
  document.getElementById('searchForm').addEventListener('submit', handleSearchSubmit);
  document.getElementById('btnResetSearch').addEventListener('click', handleResetSearch);
  document.getElementById('registerDonorForm').addEventListener('submit', handleRegisterDonorSubmit);
  document.getElementById('donationForm').addEventListener('submit', handleRecordDonationSubmit);
  document.getElementById('btnRefreshDirectory').addEventListener('click', () => {
    loadAllDonors();
    loadStats();
    showToast('Donor directory refreshed', 'info');
  });

  // Initial search with empty filters to show all eligible donors
  executeDonorSearch('', '');
});

// Tab Navigation
function initTabs() {
  const tabButtons = document.querySelectorAll('.tab-btn');
  tabButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      tabButtons.forEach(b => b.classList.remove('active'));
      document.querySelectorAll('.tab-pane').forEach(p => p.classList.remove('active'));

      btn.classList.add('active');
      const targetPane = document.getElementById(btn.dataset.tab);
      if (targetPane) targetPane.classList.add('active');

      // Refresh specific views on tab switch
      if (btn.dataset.tab === 'tabMatrix') loadBloodGroupMatrix();
      if (btn.dataset.tab === 'tabDirectory') loadAllDonors();
    });
  });
}

// Set maximum allowed date for donation pickers to today (no future dates)
function initDatePickers() {
  const today = new Date().toISOString().split('T')[0];
  const regLast = document.getElementById('regLastDonation');
  const donDate = document.getElementById('donationDate');

  if (regLast) regLast.setAttribute('max', today);
  if (donDate) {
    donDate.setAttribute('max', today);
    donDate.value = today; // default to today
  }
}

// Fetch Blood Groups
async function loadBloodGroups() {
  try {
    const res = await fetch(`${API_BASE}/blood-groups`);
    if (!res.ok) throw new Error('Failed to fetch blood groups');
    bloodGroupsList = await res.json();

    populateBloodGroupDropdowns();
  } catch (err) {
    console.error('Error loading blood groups:', err);
    showToast('Failed to load blood groups from server', 'error');
  }
}

function populateBloodGroupDropdowns() {
  const searchSelect = document.getElementById('searchBloodGroup');
  const regSelect = document.getElementById('regBloodGroup');

  if (searchSelect) {
    searchSelect.innerHTML = '<option value="">All Blood Groups</option>';
    bloodGroupsList.forEach(bg => {
      searchSelect.innerHTML += `<option value="${bg.name}">${bg.name} (${bg.availableDonors} available)</option>`;
    });
  }

  if (regSelect) {
    regSelect.innerHTML = '<option value="">Select Blood Group *</option>';
    bloodGroupsList.forEach(bg => {
      regSelect.innerHTML += `<option value="${bg.id}">${bg.name} - ${bg.description}</option>`;
    });
  }
}

// Load Dashboard Quick Stats
async function loadStats() {
  try {
    const res = await fetch(`${API_BASE}/donors/count-summary`);
    if (!res.ok) throw new Error('Failed to load counts');
    const data = await res.json();

    document.getElementById('statTotalDonors').textContent = data.totalRegistered;
    document.getElementById('statAvailableDonors').textContent = data.totalAvailable;
    const inCooldown = Math.max(0, data.totalRegistered - data.totalAvailable);
    document.getElementById('statCooldownDonors').textContent = inCooldown;
    document.getElementById('statBloodGroups').textContent = bloodGroupsList.length || 8;
  } catch (err) {
    console.error('Error fetching stats:', err);
  }
}

// Quick city filter buttons
function setSearchCity(cityName) {
  document.getElementById('searchCity').value = cityName;
  document.getElementById('btnExecuteSearch').click();
}

// TAB 1: Search Donors
function handleSearchSubmit(e) {
  e.preventDefault();
  const bg = document.getElementById('searchBloodGroup').value;
  const city = document.getElementById('searchCity').value.trim();
  executeDonorSearch(bg, city);
}

function handleResetSearch() {
  document.getElementById('searchBloodGroup').value = '';
  document.getElementById('searchCity').value = '';
  executeDonorSearch('', '');
}

async function executeDonorSearch(bloodGroup, city) {
  const tbody = document.getElementById('searchResultsBody');
  tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding: 2rem;">Searching eligible donors...</td></tr>`;

  try {
    const params = new URLSearchParams();
    if (bloodGroup) params.append('bloodGroup', bloodGroup);
    if (city) params.append('city', city);

    const res = await fetch(`${API_BASE}/donors/search?${params.toString()}`);
    if (!res.ok) throw new Error('Search failed');

    const donors = await res.json();
    document.getElementById('searchResultsCount').textContent = `${donors.length} Eligible Found`;

    if (donors.length === 0) {
      tbody.innerHTML = `
        <tr>
          <td colspan="7">
            <div class="empty-state">
              <div class="empty-icon">🔍</div>
              <h4>No Eligible Donors Found</h4>
              <p>No active donors outside the 90-day cooldown match "${bloodGroup || 'Any Blood Group'}" in "${city || 'Any City'}".</p>
            </div>
          </td>
        </tr>`;
      return;
    }

    tbody.innerHTML = donors.map(d => `
      <tr>
        <td><strong>${escapeHtml(d.name)}</strong></td>
        <td><span class="badge badge-blood">${escapeHtml(d.bloodGroup)}</span></td>
        <td>${escapeHtml(d.city)}</td>
        <td><a href="tel:${escapeHtml(d.phone)}" style="color:var(--primary); font-weight:700; text-decoration:none;">📞 ${escapeHtml(d.phone)}</a></td>
        <td><small style="color:var(--text-muted);">${escapeHtml(d.email)}</small></td>
        <td>
          <span class="badge badge-success">
            <span class="pulse-dot"></span> Available Now
          </span>
        </td>
        <td>
          <button class="btn btn-primary btn-sm" onclick="openDirectDonationModal(${d.id})">
            Record Donation
          </button>
        </td>
      </tr>
    `).join('');

  } catch (err) {
    console.error('Error during search:', err);
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:var(--danger); padding:2rem;">Error executing search. Please verify backend server is running.</td></tr>`;
  }
}

// TAB 2: Load All Donors Directory
async function loadAllDonors() {
  const tbody = document.getElementById('donorsTableBody');
  const donorSelect = document.getElementById('donationDonorSelect');

  try {
    const res = await fetch(`${API_BASE}/donors`);
    if (!res.ok) throw new Error('Failed to load donors');
    allDonorsList = await res.json();

    // Populate donation dropdown
    if (donorSelect) {
      donorSelect.innerHTML = '<option value="">Choose registered donor...</option>' +
        allDonorsList
          .filter(d => d.active)
          .map(d => `<option value="${d.id}">${escapeHtml(d.name)} (${d.bloodGroup} - ${d.city}) ${d.inCooldown ? '[In Cooldown]' : '[Ready]'}</option>`)
          .join('');
    }

    if (allDonorsList.length === 0) {
      tbody.innerHTML = `<tr><td colspan="8" class="empty-state"><div class="empty-icon">👥</div><h4>No Donors Registered Yet</h4></td></tr>`;
      return;
    }

    tbody.innerHTML = allDonorsList.map(d => {
      let statusBadge = '';
      if (!d.active) {
        statusBadge = `<span class="badge badge-inactive">Inactive / Deactivated</span>`;
      } else if (d.inCooldown) {
        statusBadge = `<span class="badge badge-warning" title="Next eligible: ${d.nextEligibleDate}">⏳ In Cooldown (${d.daysUntilEligible}d left)</span>`;
      } else {
        statusBadge = `<span class="badge badge-success"><span class="pulse-dot"></span> Available</span>`;
      }

      const lastDon = d.lastDonationDate ? d.lastDonationDate : '<em style="color:var(--text-light)">Never donated</em>';

      return `
        <tr>
          <td>#${d.id}</td>
          <td><strong>${escapeHtml(d.name)}</strong></td>
          <td><span class="badge badge-blood">${escapeHtml(d.bloodGroup)}</span></td>
          <td>${escapeHtml(d.city)}</td>
          <td>
            <div>${escapeHtml(d.phone)}</div>
            <small style="color:var(--text-muted);">${escapeHtml(d.email)}</small>
          </td>
          <td>${lastDon}</td>
          <td>${statusBadge}</td>
          <td>
            <div style="display:flex; gap:0.4rem;">
              <button class="btn btn-secondary btn-sm" onclick="viewDonorHistory(${d.id}, '${escapeHtml(d.name)}')">History</button>
              ${d.active ? `<button class="btn btn-danger-outline btn-sm" onclick="deactivateDonor(${d.id})">Deactivate</button>` : ''}
            </div>
          </td>
        </tr>
      `;
    }).join('');

  } catch (err) {
    console.error('Error fetching donors:', err);
    tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; color:var(--danger);">Error fetching donors.</td></tr>`;
  }
}

// TAB 3: Register Donor Submit
async function handleRegisterDonorSubmit(e) {
  e.preventDefault();

  const payload = {
    name: document.getElementById('regName').value.trim(),
    email: document.getElementById('regEmail').value.trim(),
    phone: document.getElementById('regPhone').value.trim(),
    bloodGroupId: parseInt(document.getElementById('regBloodGroup').value),
    city: document.getElementById('regCity').value.trim(),
    lastDonationDate: document.getElementById('regLastDonation').value || null
  };

  try {
    const res = await fetch(`${API_BASE}/donors`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    const data = await res.json();

    if (!res.ok) {
      if (res.status === 400 && data.errors) {
        const errorMessages = Object.values(data.errors).join(', ');
        throw new Error(errorMessages);
      } else {
        throw new Error(data.message || 'Registration failed');
      }
    }

    showToast(`Donor "${data.name}" registered successfully!`, 'success');
    document.getElementById('registerDonorForm').reset();
    initDatePickers();

    // Refresh application state
    loadStats();
    loadAllDonors();
    loadBloodGroupMatrix();
    executeDonorSearch('', '');

    // Switch to directory tab to view new donor
    document.getElementById('tabBtnDirectory').click();

  } catch (err) {
    console.error('Registration error:', err);
    showToast(err.message, 'error');
  }
}

// TAB 4: Record Donation Submit
async function handleRecordDonationSubmit(e) {
  e.preventDefault();

  const donorId = parseInt(document.getElementById('donationDonorSelect').value);
  const donationDate = document.getElementById('donationDate').value;
  const notes = document.getElementById('donationNotes').value.trim();

  if (!donorId || !donationDate) {
    showToast('Please select a donor and donation date', 'error');
    return;
  }

  const payload = {
    donorId: donorId,
    donationDate: donationDate,
    notes: notes || null
  };

  try {
    const res = await fetch(`${API_BASE}/donations`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    const data = await res.json();

    if (!res.ok) {
      throw new Error(data.message || 'Failed to record donation');
    }

    showToast(`Donation recorded! Donor entered 90-day cooldown until ${data.nextEligibleDate}.`, 'success');
    document.getElementById('donationForm').reset();
    initDatePickers();

    // Refresh application views
    loadStats();
    loadAllDonors();
    loadRecentDonations();
    loadBloodGroupMatrix();
    executeDonorSearch('', '');

  } catch (err) {
    console.error('Donation error:', err);
    showToast(err.message, 'error');
  }
}

// Load Recent Donations
async function loadRecentDonations() {
  const tbody = document.getElementById('recentDonationsBody');
  try {
    const res = await fetch(`${API_BASE}/donations`);
    if (!res.ok) throw new Error('Failed to load donations');
    const records = await res.json();

    if (records.length === 0) {
      tbody.innerHTML = `<tr><td colspan="7" class="empty-state"><div class="empty-icon">💉</div><h4>No Donations Logged Yet</h4></td></tr>`;
      return;
    }

    tbody.innerHTML = records.slice(0, 10).map(r => `
      <tr>
        <td>#${r.id}</td>
        <td><strong>${escapeHtml(r.donorName)}</strong></td>
        <td><span class="badge badge-blood">${escapeHtml(r.donorBloodGroup)}</span></td>
        <td>${escapeHtml(r.donorCity || '-')}</td>
        <td>${r.donationDate}</td>
        <td><span class="badge badge-warning">${r.nextEligibleDate}</span></td>
        <td><small>${escapeHtml(r.notes || 'Routine donation')}</small></td>
      </tr>
    `).join('');

  } catch (err) {
    console.error('Error fetching recent donations:', err);
  }
}

// TAB 5: Blood Group Matrix
async function loadBloodGroupMatrix() {
  const container = document.getElementById('bgMatrixContainer');
  try {
    const res = await fetch(`${API_BASE}/blood-groups`);
    if (!res.ok) throw new Error('Failed to load blood groups');
    const groups = await res.json();

    container.innerHTML = groups.map(g => {
      const percentage = g.totalDonors > 0 ? Math.round((g.availableDonors / g.totalDonors) * 100) : 0;
      return `
        <div class="bg-card">
          <div class="bg-card-header">
            <div class="bg-symbol">${escapeHtml(g.name)}</div>
            <span class="badge ${g.availableDonors > 0 ? 'badge-success' : 'badge-warning'}">
              ${g.availableDonors} Available
            </span>
          </div>
          <div class="bg-counts">
            <span>Registered Donors: <strong>${g.totalDonors}</strong></span>
            <span>Available: <strong>${g.availableDonors}</strong></span>
          </div>
          <div class="progress-track">
            <div class="progress-fill" style="width: ${percentage}%"></div>
          </div>
          <div class="bg-desc">${escapeHtml(g.description || 'Compatibility information')}</div>
        </div>
      `;
    }).join('');

  } catch (err) {
    console.error('Error loading matrix:', err);
  }
}

// Deactivate Donor
async function deactivateDonor(id) {
  if (!confirm(`Are you sure you want to deactivate donor #${id}? They will no longer appear in search results.`)) {
    return;
  }

  try {
    const res = await fetch(`${API_BASE}/donors/${id}`, { method: 'DELETE' });
    if (!res.ok) throw new Error('Deactivation failed');

    showToast(`Donor #${id} deactivated successfully`, 'info');
    loadStats();
    loadAllDonors();
    loadBloodGroupMatrix();
    executeDonorSearch('', '');
  } catch (err) {
    console.error('Deactivate error:', err);
    showToast(err.message, 'error');
  }
}

// View Donor Donation History Modal
async function viewDonorHistory(donorId, donorName) {
  const modal = document.getElementById('historyModal');
  const title = document.getElementById('historyModalTitle');
  const body = document.getElementById('historyModalBody');

  title.textContent = `Donation History: ${donorName} (Donor #${donorId})`;
  body.innerHTML = '<p style="text-align:center;">Loading donation records...</p>';
  modal.classList.add('active');

  try {
    const res = await fetch(`${API_BASE}/donors/${donorId}/donations`);
    if (!res.ok) throw new Error('Failed to load history');
    const donations = await res.json();

    if (donations.length === 0) {
      body.innerHTML = `
        <div class="empty-state">
          <div class="empty-icon">📋</div>
          <h4>No Recorded Donations</h4>
          <p>This donor has not recorded any blood donations in the system yet.</p>
        </div>`;
      return;
    }

    body.innerHTML = `
      <table style="width:100%;">
        <thead>
          <tr>
            <th>Date</th>
            <th>Next Eligible Date</th>
            <th>Notes</th>
            <th>Logged At</th>
          </tr>
        </thead>
        <tbody>
          ${donations.map(d => `
            <tr>
              <td><strong>${d.donationDate}</strong></td>
              <td><span class="badge badge-warning">${d.nextEligibleDate}</span></td>
              <td>${escapeHtml(d.notes || 'None')}</td>
              <td><small style="color:var(--text-muted);">${d.createdAt ? d.createdAt.split('T')[0] : '-'}</small></td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    `;
  } catch (err) {
    body.innerHTML = `<p style="color:var(--danger); text-align:center;">Error loading donation records.</p>`;
  }
}

function closeHistoryModal() {
  document.getElementById('historyModal').classList.remove('active');
}

// Shortcut from search table to record donation for a specific donor
function openDirectDonationModal(donorId) {
  document.getElementById('tabBtnDonation').click();
  const select = document.getElementById('donationDonorSelect');
  if (select) {
    select.value = donorId;
  }
}

// Toast Alert Utility
function showToast(message, type = 'info') {
  const container = document.getElementById('toastContainer');
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;

  const icon = type === 'success' ? '✅' : type === 'error' ? '❌' : 'ℹ️';
  toast.innerHTML = `<span>${icon}</span><div>${escapeHtml(message)}</div>`;

  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(100%)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 4500);
}

// HTML escape helper
function escapeHtml(str) {
  if (str === null || str === undefined) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}
