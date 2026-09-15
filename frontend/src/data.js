export const photos = {
  hero: '/images/hero.jpg',
  bali: '/images/bali.jpg',
  tokyo: '/images/tokyo.jpg',
  alps: '/images/alps.jpg',
};

export const samplePackages = [
  { packageId: 'bali', name: 'Bali Wellness Escape', destination: 'Bali, Indonesia', description: 'Slow mornings, lush rice terraces and a little time just for you. Discover the quieter side of Bali with a restorative island escape.', image: photos.bali, days: 7, price: 1299 },
  { packageId: 'tokyo', name: 'Tokyo Food & Culture', destination: 'Tokyo, Japan', description: 'Follow your curiosity through lantern-lit streets, neighbourhood kitchens and serene temples. A taste of Tokyo, beyond the ordinary.', image: photos.tokyo, days: 10, price: 2450 },
  { packageId: 'alps', name: 'Swiss Alps Adventure', destination: 'Swiss Alps, Switzerland', description: 'Fresh mountain air, turquoise lakes and trails with unforgettable views. Find your sense of adventure in the heart of the Swiss Alps.', image: photos.alps, days: 5, price: 1890 },
].map(item => ({ ...item, sample: true, departures: [] }));

export function packageImage(item) {
  if (item.image) return item.image;
  if (/japan|tokyo/i.test(item.destination)) return photos.tokyo;
  if (/swiss|alps|mountain/i.test(item.destination)) return photos.alps;
  return photos.bali;
}

export function matchingDepartures(item, filters) {
  const today = new Date().toLocaleDateString('en-CA');
  return item.departures.filter(d => d.status === 'AVAILABLE' && d.startDate >= today
    && (!filters.date || d.startDate === filters.date)
    && (!filters.budget || d.price <= Number(filters.budget))
    && (!filters.travellers || (d.availableCapacity != null && d.availableCapacity >= Number(filters.travellers))));
}
