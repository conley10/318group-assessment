export const photos = {
  hero: '/images/hero.jpg',
  bali: '/images/bali.jpg',
  tokyo: '/images/tokyo.jpg',
  italy: '/images/italy.jpg',
  greece: '/images/greece.jpg',
  newzealand: '/images/newzealand.jpg',
  fiji: '/images/fiji.jpg',
  alps: '/images/alps.jpg',
};

export const samplePackages = [
  { packageId: 'bali', name: 'Bali Wellness Escape', destination: 'Bali, Indonesia', description: 'Slow mornings, lush rice terraces and a little time just for you. Discover the quieter side of Bali with a restorative island escape.', image: photos.bali, days: 7, price: 1299 },
  { packageId: 'tokyo', name: 'Tokyo Food & Culture', destination: 'Tokyo, Japan', description: 'Follow your curiosity through lantern-lit streets, neighbourhood kitchens and serene temples. A taste of Tokyo, beyond the ordinary.', image: photos.tokyo, days: 10, price: 2450 },
  { packageId: 'alps', name: 'Swiss Alps Adventure', destination: 'Swiss Alps, Switzerland', description: 'Fresh mountain air, turquoise lakes and trails with unforgettable views. Find your sense of adventure in the heart of the Swiss Alps.', image: photos.alps, days: 5, price: 1890 },
].map(item => ({ ...item, sample: true, departures: [] }));

export function packageImage(item) {
  if (item.image) return item.image;

  const destination = item.destination || '';

  if (/japan|tokyo/i.test(destination)) {
    return photos.tokyo;
  }

  if (/italy|rome|florence|venice/i.test(destination)) {
    return photos.italy;
  }

  if (/greece|athens|mykonos|santorini/i.test(destination)) {
    return photos.greece;
  }

  if (/indonesia|bali/i.test(destination)) {
    return photos.bali;
  }

  if (/new zealand|queenstown/i.test(destination)) {
    return photos.newzealand;
  }

  if (/fiji/i.test(destination)) {
    return photos.fiji;
  }

  if (/swiss|alps|mountain/i.test(destination)) {
    return photos.alps;
  }

  return photos.bali;
}

export function matchingDepartures(item, filters) {
  const today = new Date().toLocaleDateString('en-CA');

  return item.departures.filter(
    d =>
      d.status === 'AVAILABLE' &&
      d.startDate >= today &&
      (!filters.date ||
        d.startDate === filters.date) &&
      (!filters.budget ||
        d.price <= Number(filters.budget))
  );
}
