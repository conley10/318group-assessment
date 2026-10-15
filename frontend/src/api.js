const catalogue = import.meta.env.VITE_CATALOGUE_API_URL || '/catalogue-api';
const assistant = import.meta.env.VITE_ASSISTANT_API_URL || '/assistant-api';
const booking = import.meta.env.VITE_BOOKING_API_URL || '/booking-api';
const notification = import.meta.env.VITE_NOTIFICATION_API_URL || '/notification-api';
const analytics = import.meta.env.VITE_ANALYTICS_API_URL || '/analytics-api';

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

export async function createBooking(bookingData) {
  const response = await fetch(`${booking}/bookings`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(bookingData),
  });

  if (!response.ok) {
    throw new Error(`Failed to create booking: ${response.status}`);
  }

  return response.json();
}

export async function getBookings() {
  const response = await fetch(`${booking}/bookings`);

  if (!response.ok) {
    throw new Error(`Failed to load bookings: ${response.status}`);
  }

  return response.json();
}

export async function getBooking(bookingId) {
  const response = await fetch(
    `${booking}/bookings/${bookingId}`
  );

  if (!response.ok) {
    throw new Error(
      `Failed to load booking: ${response.status}`
    );
  }

  return response.json();
}

export async function getAnalytics() {
  const response = await fetch(`${analytics}/analytics`);

  if (!response.ok) {
    throw new Error(`Failed to load analytics: ${response.status}`);
  }

  return response.json();
}

export async function getNotifications() {
  const response = await fetch(`${notification}/notifications`);

  if (!response.ok) {
    throw new Error(`Failed to load notifications: ${response.status}`);
  }

  return response.json();
}

export function askAssistant(message) {
  return request(`${assistant}/assistant/recommend`, {
    method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ message }),
  });
}
