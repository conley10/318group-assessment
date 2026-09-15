const catalogue = import.meta.env.VITE_CATALOGUE_API_URL || '/catalogue-api';
const assistant = import.meta.env.VITE_ASSISTANT_API_URL || '/assistant-api';

async function request(url, options = {}) {
  const response = await fetch(url, { ...options, signal: AbortSignal.timeout(45000) });
  if (!response.ok) throw new Error('The service is unavailable. Please try again shortly.');
  return response.json();
}

export async function getPackages() {
  const packages = await request(`${catalogue}/packages`);
  return Promise.all(packages.map(async item => {
    try {
      const departures = await request(`${catalogue}/packages/${item.packageId}/departures`);
      return { ...item, departures: await Promise.all(departures.map(async departure => {
        try {
          const availability = await request(`${catalogue}/departures/${departure.departureId}/availability`);
          return { ...departure, ...availability };
        } catch { return { ...departure, availableCapacity: null }; }
      })) };
    } catch { return { ...item, departures: [], departureError: true }; }
  }));
}

export function askAssistant(message) {
  return request(`${assistant}/assistant/recommend`, {
    method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ message }),
  });
}
