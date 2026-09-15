import { test, expect } from '@playwright/test';

test('offline catalogue shows labelled samples, functional search and accessible details', async ({ page }) => {
  await page.route('**/catalogue-api/**', route => route.fulfill({ status: 503, body: '{}' }));
  await page.goto('/');
  await expect(page.getByRole('heading', { name: 'Featured Packages' })).toBeVisible();
  await expect(page.locator('article')).toHaveCount(3);
  await expect(page.getByText('Showing sample trips')).toBeVisible();
  await page.getByPlaceholder('Where to?').fill('tokyo');
  await page.getByRole('button', { name: 'Search Trips' }).click();
  await expect(page.locator('article')).toHaveCount(1);
  await page.getByRole('button', { name: 'View Details' }).click();
  await expect(page.getByRole('dialog')).toBeVisible();
  await expect(page.getByText('This is a sample itinerary')).toBeVisible();
  await page.keyboard.press('Escape');
  await expect(page.getByRole('dialog')).toHaveCount(0);
  await page.getByLabel('Departure date', { exact: true }).fill('2030-05-01');
  await page.getByRole('button', { name: 'Search Trips' }).click();
  await expect(page.getByText('A different adventure awaits')).toBeVisible();
});

test('live filters use departure price and remaining capacity and AI sends form preferences', async ({ page }) => {
  await page.route('**/catalogue-api/**', route => {
    const path = new URL(route.request().url()).pathname;
    const data = path.endsWith('/availability') ? { availableCapacity: 2 }
      : path.endsWith('/departures') ? [{ departureId: 10, startDate: '2030-05-01', endDate: '2030-05-08', price: 1400, capacity: 20, status: 'AVAILABLE' }]
      : [{ packageId: 1, name: 'Live Tokyo Escape', destination: 'Tokyo, Japan', description: 'A live catalogue trip.' }];
    return route.fulfill({ json: data });
  });
  await page.route('**/assistant-api/**', async route => {
    expect(route.request().postDataJSON().message).toContain('food');
    await route.fulfill({ json: { response: 'Consider the Live Tokyo Escape.' } });
  });
  await page.goto('/');
  await expect(page.getByRole('heading', { name: 'Live Tokyo Escape' })).toBeVisible();
  await page.getByRole('combobox', { name: 'Travellers', exact: true }).selectOption('3');
  await page.getByRole('button', { name: 'Search Trips' }).click();
  await expect(page.locator('article')).toHaveCount(0);
  await page.getByRole('combobox', { name: 'Travellers', exact: true }).selectOption('2');
  await page.getByLabel('Departure date', { exact: true }).fill('2030-05-01');
  await page.getByRole('combobox', { name: 'Budget per person' }).selectOption('1500');
  await page.getByRole('button', { name: 'Search Trips' }).click();
  await expect(page.locator('article')).toHaveCount(1);
  await expect(page.locator('article')).toContainText('1,400');
  await page.getByLabel('Interests for your AI request').fill('food');
  await page.getByRole('button', { name: 'Ask AI Instead' }).click();
  await page.getByRole('button', { name: 'Ask Voyage' }).click();
  await expect(page.getByText('Consider the Live Tokyo Escape.')).toBeVisible();
});

test('desktop and mobile layouts have no horizontal overflow', async ({ page }) => {
  await page.route('**/catalogue-api/**', route => route.fulfill({ json: [] }));
  for (const width of [1440, 768, 375]) {
    await page.setViewportSize({ width, height: 1050 });
    await page.goto('/');
    await expect(page.locator('article')).toHaveCount(3);
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true);
    await page.screenshot({ path: `test-results/voyage-${width}.png`, fullPage: true });
  }
});
