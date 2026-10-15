import React, { useEffect, useRef, useState } from 'react';
import { createRoot } from 'react-dom/client';
import {
  ArrowRight,
  CalendarDays,
  Check,
  Clock3,
  Compass,
  MapPin,
  Search,
  Send,
  Sparkles,
  Users,
  Wallet,
  X
} from 'lucide-react';

import {
  getPackages,
  askAssistant,
  createBooking,
  getBookings,
  getBooking,
  getNotifications,
  getAnalytics
} from './api';

import {
  photos,
  samplePackages,
  packageImage,
  matchingDepartures
} from './data';

import './styles.css';

const blankFilters = {
  destination: '',
  date: '',
  travellers: '',
  budget: '',
  interests: ''
};

const money = value =>
  new Intl.NumberFormat('en-US', {
    maximumFractionDigits: 0
  }).format(value);

function Modal({ title, children, close }) {
  const ref = useRef(null);

  useEffect(() => {
    const dialog = ref.current;
    dialog.showModal();

    return () => dialog.close();
  }, []);

  return (
    <dialog
      ref={ref}
      onCancel={close}
      onClick={e => {
        if (e.target === ref.current) close();
      }}
      aria-labelledby="dialog-title"
    >
      <div className="modal-heading">
        <h2 id="dialog-title">{title}</h2>

        <button
          className="icon-button"
          onClick={close}
          aria-label="Close dialog"
        >
          <X size={21} />
        </button>
      </div>

      {children}
    </dialog>
  );
}

function formatAiAnswer(text) {
  if (!text) return null;

  return text.split('\n').map((line, lineIndex) => {
    const parts = line.split(/(\*\*.*?\*\*)/g);

    return (
      <React.Fragment key={lineIndex}>
        {parts.map((part, partIndex) =>
          part.startsWith('**') &&
          part.endsWith('**') ? (
            <strong key={partIndex}>
              {part.slice(2, -2)}
            </strong>
          ) : (
            <React.Fragment key={partIndex}>
              {part}
            </React.Fragment>
          )
        )}
        <br />
      </React.Fragment>
    );
  });
}

function App() {
  const [packages, setPackages] = useState([]);
  const [state, setState] = useState('loading');

  const [filters, setFilters] = useState(blankFilters);
  const [applied, setApplied] = useState(null);

  const [showAll, setShowAll] = useState(false);
  const [modal, setModal] = useState(null);

  // AI
  const [message, setMessage] = useState('');
  const [answer, setAnswer] = useState('');
  const [aiError, setAiError] = useState('');
  const [asking, setAsking] = useState(false);

  // Booking creation
  const [booking, setBooking] = useState(false);
  const [bookingMessage, setBookingMessage] = useState('');
  const [bookingError, setBookingError] = useState('');

  // My Bookings
  const [bookings, setBookings] = useState([]);
  const [bookingsLoading, setBookingsLoading] = useState(false);
  const [bookingsError, setBookingsError] = useState('');
  const [pendingBooking, setPendingBooking] = useState(null);

  // Notifications
const [notifications, setNotifications] = useState([]);
const [notificationsLoading, setNotificationsLoading] = useState(false);
const [notificationsError, setNotificationsError] = useState('');

// Analytics
const [analytics, setAnalytics] = useState(null);
const [analyticsLoading, setAnalyticsLoading] = useState(false);
const [analyticsError, setAnalyticsError] = useState('');

  async function load() {
    setState('loading');

    try {
      const data = await getPackages();
      setPackages(data);
      setState(data.length ? 'live' : 'empty');
    } catch {
      setPackages([]);
      setState('offline');
    }
  }

  useEffect(() => {
    load();
  }, []);

  const demo =
    state === 'offline' ||
    state === 'empty';

  const source =
    demo ? samplePackages : packages;

  const results = source.filter(item => {
    if (!applied) return true;

    if (
      !item.destination
        .toLowerCase()
        .includes(
          applied.destination
            .trim()
            .toLowerCase()
        )
    ) {
      return false;
    }

    if (item.sample) {
      return (
        !applied.date &&
        !applied.travellers &&
        (!applied.budget ||
          item.price <=
            Number(applied.budget))
      );
    }

    return (
      !(
        applied.date ||
        applied.travellers ||
        applied.budget
      ) ||
      matchingDepartures(
        item,
        applied
      ).length > 0
    );
  });

  const visible =
    applied || showAll
      ? results
      : results.slice(0, 3);

  function update(event) {
    setFilters(current => ({
      ...current,
      [event.target.name]:
        event.target.value
    }));
  }

  function search(event) {
    event.preventDefault();

    setApplied({ ...filters });

    document
      .getElementById('packages')
      .scrollIntoView({
        behavior: 'smooth',
        block: 'start'
      });
  }

  function openAssistant(prefill = false) {
    if (prefill) {
      setMessage(
        `Help me plan a trip${
          filters.destination
            ? ` to ${filters.destination}`
            : ''
        }${
          filters.date
            ? ` departing ${filters.date}`
            : ''
        }${
          filters.travellers
            ? ` for ${filters.travellers} travellers`
            : ''
        }${
          filters.budget
            ? ` with a budget of ${filters.budget} per person`
            : ''
        }${
          filters.interests
            ? `. My interests: ${filters.interests}`
            : ''
        }.`
      );
    }

    setModal('ai');
  }

  async function submitAi(event) {
    event.preventDefault();

    if (!message.trim() || asking)
      return;

    setAsking(true);
    setAiError('');
    setAnswer('');

    try {
      const data =
        await askAssistant(
          message.trim()
        );

      setAnswer(data.response);
    } catch {
      setAiError(
        'Your travel assistant is unavailable right now. Please try again in a moment.'
      );
    } finally {
      setAsking(false);
    }
  }

function prepareBooking(packageItem, departure) {
  const quantity = Number(filters.travellers) || 1;

  setBookingMessage('');
  setBookingError('');

  setPendingBooking({
    packageItem,
    departure,
    quantity,
    totalPrice: Number(departure.price) * quantity
  });

  setModal('confirm-booking');
}

  async function bookTrip(
    packageItem,
    departure
  ) {
    if (booking) return;

    setBooking(true);
    setBookingMessage('');
    setBookingError('');

    try {
      const quantity =
        Number(pendingBooking?.quantity) || 1;

      const totalPrice =
        Number(pendingBooking?.totalPrice) ||
        Number(departure.price) * quantity;

      const bookingData = {
        customerId:
          '11111111-1111-1111-1111-111111111111',

        packageId:
          packageItem.packageId,

        departureId:
          departure.departureId,

        quantity,

        totalPrice
      };

      console.log(
        'Creating booking:',
        bookingData
      );

      const result =
        await createBooking(
          bookingData
        );

      console.log(
        'Booking response:',
        result
      );

setBookingMessage(
  `Your booking has been submitted. Status: ${result.status}`
);

setPendingBooking({
  ...pendingBooking,
  result
});

setModal('booking-result');
    } catch (error) {
      console.error(
        'Booking failed:',
        error
      );

      setBookingError(
        'Unable to create your booking. Please try again.'
      );
    } finally {
      setBooking(false);
    }
  }

  useEffect(() => {
  const result = pendingBooking?.result;

  if (
    modal !== 'booking-result' ||
    !result?.bookingId ||
    result.status === 'CONFIRMED' ||
    result.status === 'FAILED'
  ) {
    return;
  }

  let cancelled = false;
  let attempts = 0;

  const checkBookingStatus = async () => {
    attempts += 1;

    try {
      const updatedBooking = await getBooking(
        result.bookingId
      );

      if (cancelled) return;

      setPendingBooking(current => {
        if (!current) return current;

        return {
          ...current,
          result: updatedBooking
        };
      });

      if (
        updatedBooking.status === 'CONFIRMED' ||
        updatedBooking.status === 'FAILED'
      ) {
        return;
      }

      if (attempts < 10) {
        setTimeout(checkBookingStatus, 1000);
      }
    } catch (error) {
      console.error(
        'Failed to refresh booking status:',
        error
      );

      if (!cancelled && attempts < 10) {
        setTimeout(checkBookingStatus, 1000);
      }
    }
  };

  const timer = setTimeout(
    checkBookingStatus,
    1000
  );

  return () => {
    cancelled = true;
    clearTimeout(timer);
  };
}, [
  modal,
  pendingBooking?.result?.bookingId,
  pendingBooking?.result?.status
]);

  async function openBookings() {
    setModal('bookings');
    setBookingsLoading(true);
    setBookingsError('');
    setBookings([]);

    try {
      const data =
        await getBookings();

      setBookings(data);
    } catch (error) {
      console.error(
        'Failed to load bookings:',
        error
      );

      setBookingsError(
        'Unable to load your bookings right now.'
      );
    } finally {
      setBookingsLoading(false);
    }
  }
  
async function openNotifications() {
  setModal('notifications');
  setNotificationsLoading(true);
  setNotificationsError('');
  setNotifications([]);

  try {
    const data = await getNotifications();
    setNotifications(data);
  } catch (error) {
    console.error('Failed to load notifications:', error);

    setNotificationsError(
      'Unable to load your notifications right now.'
    );
  } finally {
    setNotificationsLoading(false);
  }
}

async function openAnalytics() {
  setModal('analytics');
  setAnalyticsLoading(true);
  setAnalyticsError('');

  try {
    const data = await getAnalytics();
    setAnalytics(data);
  } catch (error) {
    console.error('Failed to load analytics:', error);

    setAnalyticsError(
      'Unable to load booking analytics right now.'
    );
  } finally {
    setAnalyticsLoading(false);
  }
}

  function findPackage(packageId) {
    return packages.find(
      item =>
        String(item.packageId) ===
        String(packageId)
    );
  }

  return (
    <>
      <a
        className="skip-link"
        href="#main"
      >
        Skip to content
      </a>

      <header className="header">
        <a
          className="brand"
          href="./"
          aria-label="Voyage home"
        >
          Voyage
          <span className="brand-dot">
            .
          </span>
        </a>

        <nav aria-label="Main navigation">
          <a
            className="active"
            href="#main"
            aria-current="page"
          >
            Explore
          </a>

          <button
            onClick={openBookings}
          >
            My Bookings
          </button>

          <button onClick={openNotifications}>
  Notifications
</button>

<button onClick={openAnalytics}>
  Analytics
</button>

          <button
            onClick={() =>
              openAssistant()
            }
          >
            AI Assistant
          </button>
        </nav>

        <button
          className="profile"
          onClick={() =>
            setModal('profile')
          }
        >
          Profile
          <span className="avatar">
            V
          </span>
        </button>
      </header>

      <main id="main">
        <section
          className="hero"
          aria-labelledby="hero-title"
        >
          <img
            className="hero-photo"
            src={photos.hero}
            alt="Tropical coastline with turquoise water and lush island cliffs"
            fetchPriority="high"
          />

          <div className="hero-shade" />

          <div className="hero-copy">
            <span className="eyebrow">
              <span />
              A WORLD OF POSSIBILITIES
            </span>

            <h1 id="hero-title">
              Find your next
              unforgettable trip.
            </h1>

            <p>
              Discover curated
              destinations tailored
              just for you, powered by
              intelligent AI.
            </p>
          </div>

          <span className="photo-caption">
            <MapPin size={13} />
            Somewhere you’d rather be
          </span>
        </section>

        <form
          className="search-panel"
          onSubmit={search}
        >
          <div className="search-fields">
            <label>
              Destination

              <div className="input-wrap">
                <MapPin />

                <input
                  name="destination"
                  value={
                    filters.destination
                  }
                  onChange={update}
                  placeholder="Where to?"
                />
              </div>
            </label>

            <label>
              Departure date

              <div className="input-wrap">
                <CalendarDays />

                <input
                  aria-label="Departure date"
                  name="date"
                  type="date"
                  value={filters.date}
                  min={new Date().toLocaleDateString(
                    'en-CA'
                  )}
                  onChange={update}
                />
              </div>
            </label>

            <label>
              Travellers

              <div className="input-wrap">
                <Users />

                <select
                  name="travellers"
                  value={
                    filters.travellers
                  }
                  onChange={update}
                >
                  <option value="">
                    Add guests
                  </option>

                  {[
                    1, 2, 3, 4,
                    5, 6, 7, 8
                  ].map(n => (
                    <option
                      key={n}
                      value={n}
                    >
                      {n}{' '}
                      {n === 1
                        ? 'traveller'
                        : 'travellers'}
                    </option>
                  ))}
                </select>
              </div>
            </label>

            <label>
              Budget per person

              <div className="input-wrap">
                <Wallet />

                <select
                  name="budget"
                  value={
                    filters.budget
                  }
                  onChange={update}
                >
                  <option value="">
                    Any budget
                  </option>

                  <option value="1500">
                    Up to 1,500
                  </option>

                  <option value="2500">
                    Up to 2,500
                  </option>

                  <option value="5000">
                    Up to 5,000
                  </option>
                </select>
              </div>
            </label>
          </div>

          <div className="search-bottom">
            <input
              className="interests"
              name="interests"
              aria-label="Interests for your AI request"
              value={
                filters.interests
              }
              onChange={update}
              placeholder="Add interests for AI (e.g. food, hiking)"
            />

            <div className="search-actions">
              <button
                type="button"
                className="button secondary"
                onClick={() =>
                  openAssistant(true)
                }
              >
                <Sparkles
                  size={16}
                />
                Ask AI Instead
              </button>

              <button
                className="button primary"
                type="submit"
              >
                <Search size={16} />
                Search Trips
              </button>
            </div>
          </div>
        </form>

        <section
          className="packages-section"
          id="packages"
          aria-labelledby="packages-title"
        >
          <div className="section-heading">
            <div>
              <span className="section-kicker">
                A LITTLE INSPIRATION
              </span>

              <h2 id="packages-title">
                {applied
                  ? 'Your next adventure'
                  : showAll
                    ? 'Explore all packages'
                    : 'Featured Packages'}
              </h2>

              <p>
                {applied
                  ? `${results.length} ${
                      results.length === 1
                        ? 'trip'
                        : 'trips'
                    } to explore. Your next story starts here.`
                  : 'Extraordinary places. Thoughtfully chosen experiences.'}
              </p>
            </div>

            <button
              className="text-button"
              onClick={() => {
                setShowAll(
                  !showAll
                );
                setApplied(null);
              }}
            >
              {showAll || applied
                ? 'Back to featured'
                : 'See all'}

              <ArrowRight
                size={16}
              />
            </button>
          </div>

          {demo && (
            <div
              className="notice"
              role="status"
            >
              {state === 'offline'
                ? 'Showing sample trips while the catalogue is unavailable.'
                : 'Your catalogue is empty. Explore these sample trips for inspiration.'}{' '}

              <button onClick={load}>
                Retry catalogue
              </button>
            </div>
          )}

          {state === 'loading' ? (
            <div
              className="cards"
              aria-label="Loading trips"
              aria-busy="true"
            >
              {[1, 2, 3].map(n => (
                <div
                  className="skeleton"
                  key={n}
                />
              ))}
            </div>
          ) : (
            <div className="cards">
              {visible.map(
                (item, index) => {
                  const departures =
                    matchingDepartures(
                      item,
                      applied ||
                        blankFilters
                    ).sort(
                      (a, b) =>
                        a.price -
                        b.price
                    );

                  const first =
                    departures[0];

                  const price =
                    item.sample
                      ? item.price
                      : first?.price;

                  const days =
                    item.sample
                      ? item.days
                      : first
                        ? Math.round(
                            (Date.parse(
                              first.endDate
                            ) -
                              Date.parse(
                                first.startDate
                              )) /
                              86400000
                          )
                        : null;

                  return (
                    <article
                      className="card"
                      key={
                        item.packageId
                      }
                    >
                      <div className="card-photo">
                        <img
                          src={packageImage(
                            item
                          )}
                          alt={`Travel inspiration for ${item.destination}`}
                          loading="lazy"
                        />

                        {index === 0 &&
                          !applied && (
                            <span className="badge">
                              <Sparkles
                                size={
                                  12
                                }
                              />
                              {demo
                                ? 'Top Pick'
                                : 'Discover'}
                            </span>
                          )}

                        {item.sample && (
                          <span className="sample-badge">
                            Sample trip
                          </span>
                        )}
                      </div>

                      <div className="card-body">
                        <span className="destination">
                          {
                            item.destination
                          }
                        </span>

                        <h3>
                          {item.name}
                        </h3>

                        <p className="duration">
                          <Clock3
                            size={14}
                          />

                          {days != null
                            ? `${days} Days`
                            : 'Dates coming soon'}
                        </p>

                        <div className="card-bottom">
                          <div>
                            {price != null ? (
                              <>
                                <strong>
                                  {item.sample
                                    ? '$'
                                    : ''}
                                  {money(
                                    price
                                  )}
                                </strong>

                                <span>
                                  {' '}
                                  / person
                                </span>
                              </>
                            ) : (
                              <span>
                                Price
                                unavailable
                              </span>
                            )}
                          </div>

                          <button
                            className="text-button"
                            onClick={() => {
                              setBookingMessage(
                                ''
                              );
                              setBookingError(
                                ''
                              );
                              setModal(
                                item
                              );
                            }}
                          >
                            View Details
                            <ArrowRight
                              size={13}
                            />
                          </button>
                        </div>
                      </div>
                    </article>
                  );
                }
              )}
            </div>
          )}

          {state !== 'loading' &&
            !results.length && (
              <div className="empty-state">
                <Compass size={32} />

                <h3>
                  A different
                  adventure awaits
                </h3>

                <p>
                  Try another
                  destination or fewer
                  filters.
                  {demo &&
                    ' Sample trips do not have bookable dates or availability.'}
                </p>

                <button
                  className="button secondary"
                  onClick={() => {
                    setFilters(
                      blankFilters
                    );
                    setApplied(null);
                  }}
                >
                  Clear filters
                </button>
              </div>
            )}

          <div className="section-footnote">
            <Check size={14} />

            <span>
              A little inspiration
              today. An unforgettable
              journey tomorrow.
            </span>
          </div>
        </section>
      </main>

      <footer>
        <a
          className="brand"
          href="./"
        >
          Voyage
          <span className="brand-dot">
            .
          </span>
        </a>

        <span>
          Go somewhere that stays
          with you.
        </span>

        <span>
          Made for the curious.
        </span>
      </footer>

      <button
        className="floating-assistant"
        onClick={() =>
          openAssistant()
        }
        aria-label="Open AI travel assistant"
      >
        <Sparkles size={25} />
      </button>

      {/* AI Assistant */}
      {modal === 'ai' && (
        <Modal
          title="Your travel inspiration, unlocked."
          close={() =>
            setModal(null)
          }
        >
          <p className="modal-description">
            Tell Voyage what your
            ideal escape looks like.
            Our AI assistant will help
            you find a place to start.
          </p>

          <form
            onSubmit={submitAi}
          >
            <label
              className="ai-label"
              htmlFor="ai-message"
            >
              What do you have in
              mind?
            </label>

            <textarea
              id="ai-message"
              value={message}
              onChange={e =>
                setMessage(
                  e.target.value
                )
              }
              placeholder="A relaxing week in Bali with great food…"
              required
              maxLength={3000}
            />

            <button
              className="button primary"
              disabled={
                asking ||
                !message.trim()
              }
            >
              <Send size={16} />

              {asking
                ? 'Finding inspiration…'
                : 'Ask Voyage'}
            </button>
          </form>

          <div aria-live="polite">
            {asking && (
              <p>
                Thinking about your
                next adventure…
              </p>
            )}

            {aiError && (
              <p className="error">
                {aiError}
              </p>
            )}

            {answer && (
              <div className="ai-answer">
                {formatAiAnswer(answer)}
              </div>
            )}
          </div>

          <p className="fine-print">
            Suggestions are a
            starting point. Confirm
            prices, dates and
            availability in the
            package details.
          </p>
        </Modal>
      )}

      {/* Profile */}
      {modal === 'profile' && (
        <Modal
          title="Your Voyage profile"
          close={() =>
            setModal(null)
          }
        >
          <p className="modal-description">
            Personal profiles are
            coming soon. You can
            already explore
            destinations and plan
            with the AI assistant.
          </p>

          <button
            className="button primary"
            onClick={() =>
              setModal(null)
            }
          >
            Keep exploring
            <ArrowRight
              size={16}
            />
          </button>
        </Modal>
      )}

      {/* My Bookings */}
      {modal === 'bookings' && (
        <Modal
          title="Your journeys"
          close={() =>
            setModal(null)
          }
        >
          <p className="modal-description">
            Your bookings from the
            Voyage booking service.
          </p>

          {bookingsLoading && (
            <p>
              Loading your
              bookings…
            </p>
          )}

          {bookingsError && (
            <p
              className="error"
              role="alert"
            >
              {bookingsError}
            </p>
          )}

          {!bookingsLoading &&
            !bookingsError &&
            bookings.length ===
              0 && (
              <div className="empty-state">
                <Compass
                  size={30}
                />

                <h3>
                  No journeys yet
                </h3>

                <p>
                  Book a trip and it
                  will appear here.
                </p>
              </div>
            )}

          {!bookingsLoading &&
            !bookingsError &&
            bookings.length >
              0 && (
              <div className="departures">
                {bookings.map(
                  item => {
                    const trip =
                      findPackage(
                        item.packageId
                      );

                    return (
                      <div
                        className="departure"
                        key={
                          item.bookingId
                        }
                      >
                        <strong>
                          {trip?.name ||
                            `Package ${item.packageId}`}
                        </strong>

                        {trip?.destination && (
                          <span>
                            <MapPin
                              size={
                                13
                              }
                            />{' '}
                            {
                              trip.destination
                            }
                          </span>
                        )}

                        <span>
                          Status:{' '}
                          <strong>
                            {
                              item.status
                            }
                          </strong>
                        </span>

                        <span>
                          {item.quantity}{' '}
                          {item.quantity ===
                          1
                            ? 'traveller'
                            : 'travellers'}
                        </span>

                        <span>
                          Total:{' '}
                          <strong>
                            $
                            {money(
                              item.totalPrice
                            )}
                          </strong>
                        </span>

                        <span className="fine-print">
                          Booking ID:{' '}
                          {
                            item.bookingId
                          }
                        </span>
                      </div>
                    );
                  }
                )}
              </div>
            )}

          <button
            className="button secondary"
            onClick={
              openBookings
            }
            disabled={
              bookingsLoading
            }
          >
            {bookingsLoading
              ? 'Refreshing…'
              : 'Refresh Bookings'}
          </button>
        </Modal>
      )}

{/* Notifications */}
{modal === 'notifications' && (
  <Modal
    title="Notifications"
    close={() => setModal(null)}
  >
    <p className="modal-description">
      Updates about your Voyage bookings.
    </p>

    {notificationsLoading && (
      <p>Loading notifications…</p>
    )}

    {notificationsError && (
      <p className="error" role="alert">
        {notificationsError}
      </p>
    )}

    {!notificationsLoading &&
      !notificationsError &&
      notifications.length === 0 && (
        <div className="empty-state">
          <Check size={30} />

          <h3>No notifications yet</h3>

          <p>
            Booking updates will appear here.
          </p>
        </div>
      )}

    {!notificationsLoading &&
      !notificationsError &&
      notifications.length > 0 && (
        <div className="departures">
          {notifications.map(item => (
            <div
              className="departure"
              key={item.notificationId}
            >
              <strong>
                {item.type === 'BOOKING_CONFIRMED'
                  ? 'Booking Confirmed'
                  : item.type === 'BOOKING_FAILED'
                    ? 'Booking Failed'
                    : item.type
                        .replaceAll('_', ' ')
                        .toLowerCase()
                        .replace(/\b\w/g, letter =>
                          letter.toUpperCase()
                        )}
              </strong>

              <span>{item.message}</span>

              <span>
                Status: <strong>{item.status}</strong>
              </span>

              <span>
                {new Date(item.createdAt).toLocaleString()}
              </span>

              <span className="fine-print">
                Booking ID: {item.bookingId}
              </span>
            </div>
          ))}
        </div>
      )}

    <button
      className="button secondary"
      onClick={openNotifications}
      disabled={notificationsLoading}
    >
      {notificationsLoading
        ? 'Refreshing…'
        : 'Refresh Notifications'}
    </button>
  </Modal>
)}

{/* Analytics */}
{modal === 'analytics' && (
  <Modal
    title="Booking Analytics"
    close={() => setModal(null)}
  >
    <p className="modal-description">
      Live booking statistics from the Voyage stream analytics service.
    </p>

    {analyticsLoading && (
      <p>Loading analytics…</p>
    )}

    {analyticsError && (
      <p className="error" role="alert">
        {analyticsError}
      </p>
    )}

    {!analyticsLoading &&
      !analyticsError &&
      analytics && (
        <div className="departures">

          <div className="departure">
            <span>Total Bookings</span>
            <strong>
              {analytics.totalBookings}
            </strong>
          </div>

          <div className="departure">
            <span>Confirmed Bookings</span>
            <strong>
              {analytics.confirmedBookings}
            </strong>
          </div>

          <div className="departure">
            <span>Failed Bookings</span>
            <strong>
              {analytics.failedBookings}
            </strong>
          </div>

          <div className="departure">
            <span>Success Rate</span>
            <strong>
              {Number(
                analytics.successRate
              ).toFixed(1)}%
            </strong>
          </div>

        </div>
      )}

    <button
      className="button secondary"
      onClick={openAnalytics}
      disabled={analyticsLoading}
    >
      {analyticsLoading
        ? 'Refreshing…'
        : 'Refresh Analytics'}
    </button>

    <p className="fine-print">
      Analytics are calculated from booking events processed by the stream analytics service.
    </p>
  </Modal>
)}

{/* Confirm Booking */}
{modal === 'confirm-booking' && pendingBooking && (
  <Modal
    title="Confirm your journey"
    close={() => {
      setModal(null);
      setPendingBooking(null);
    }}
  >
    <p className="modal-description">
      Review your trip details before confirming your booking.
    </p>

    <div className="departures">
      <div className="departure">
        <span>Trip</span>
        <strong>
          {pendingBooking.packageItem.name}
        </strong>
      </div>

      <div className="departure">
        <span>Destination</span>
        <strong>
          {pendingBooking.packageItem.destination}
        </strong>
      </div>

      <div className="departure">
        <span>Departure</span>
        <strong>
          {pendingBooking.departure.startDate}
        </strong>
      </div>

      <div className="departure">
        <span>Return</span>
        <strong>
          {pendingBooking.departure.endDate}
        </strong>
      </div>

      <div className="departure">
        <span>Travellers</span>
        <strong>
          {pendingBooking.quantity}
        </strong>
      </div>

      <div className="departure">
        <span>Price per person</span>
        <strong>
          ${money(pendingBooking.departure.price)}
        </strong>
      </div>

      <div className="departure">
        <span>Total</span>
        <strong>
          ${money(pendingBooking.totalPrice)}
        </strong>
      </div>
    </div>

    {bookingError && (
      <p className="error" role="alert">
        {bookingError}
      </p>
    )}

    <div className="search-actions">
      <button
        type="button"
        className="button secondary"
        disabled={booking}
        onClick={() => {
          setModal(pendingBooking.packageItem);
          setPendingBooking(null);
        }}
      >
        Back
      </button>

      <button
        type="button"
        className="button primary"
        disabled={booking}
        onClick={() =>
          bookTrip(
            pendingBooking.packageItem,
            pendingBooking.departure
          )
        }
      >
        {booking
          ? 'Processing Booking…'
          : 'Confirm Booking'}
      </button>
    </div>

    <p className="fine-print">
      Your booking will only be submitted after you select Confirm Booking.
    </p>
  </Modal>
)}

{/* Booking Result */}
{modal === 'booking-result' &&
  pendingBooking?.result && (
    <Modal
      title={
        pendingBooking.result.status === 'FAILED'
          ? 'Booking unsuccessful'
          : pendingBooking.result.status === 'CONFIRMED'
            ? 'Journey booked'
            : 'Booking submitted'
      }
      close={() => {
        setModal(null);
        setPendingBooking(null);
      }}
    >
      <div className="empty-state">
        {pendingBooking.result.status === 'FAILED' ? (
          <X size={32} />
        ) : pendingBooking.result.status === 'CONFIRMED' ? (
          <Check size={32} />
        ) : (
          <Clock3 size={32} />
        )}

        <h3>
          {pendingBooking.result.status === 'FAILED'
            ? 'Your booking could not be completed'
            : pendingBooking.result.status === 'CONFIRMED'
              ? 'Your journey is confirmed'
              : 'Your booking is being processed'}
        </h3>

        <p>
          {pendingBooking.packageItem.name}
        </p>

        <p>
          Booking status:{' '}
          <strong>
            {pendingBooking.result.status}
          </strong>
        </p>

        <p>
          Total: $
          {money(pendingBooking.totalPrice)}
        </p>

        <span className="fine-print">
          Booking ID:{' '}
          {pendingBooking.result.bookingId}
        </span>
      </div>

      <button
        className="button primary"
        onClick={() => {
          setPendingBooking(null);
          openBookings();
        }}
      >
        View My Bookings
        <ArrowRight size={16} />
      </button>
    </Modal>
)}

      {/* Package Details */}
      {modal &&
        typeof modal ===
          'object' && (
          <Modal
            title={modal.name}
            close={() =>
              setModal(null)
            }
          >
            <img
              className="detail-image"
              src={packageImage(
                modal
              )}
              alt={
                modal.destination
              }
            />

            <p className="detail-location">
              <MapPin size={16} />
              {modal.destination}
            </p>

            <p className="modal-description">
              {modal.description}
            </p>

            {modal.sample ? (
              <p className="notice">
                This is a sample
                itinerary for
                inspiration. Live
                departure dates and
                booking are not
                available.
              </p>
            ) : (
              <>
                <h3>
                  Departures
                </h3>

                {modal.departureError ? (
                  <p>
                    Departure
                    details are
                    temporarily
                    unavailable.
                  </p>
                ) : modal.departures
                    .length ? (
                  <div className="departures">
                    {modal.departures.map(
                      d => (
                        <div
                          className="departure"
                          key={
                            d.departureId
                          }
                        >
                          <strong>
                            {
                              d.startDate
                            }{' '}
                            →{' '}
                            {
                              d.endDate
                            }
                          </strong>

                          <span>
                            {money(
                              d.price
                            )}{' '}
                            per person ·{' '}
                            {d.status
                              .toLowerCase()
                              .replaceAll(
                                '_',
                                ' '
                              )}
                          </span>

                          <span>
                            {d.availableCapacity ==
                            null
                              ? 'Availability unconfirmed'
                              : `${d.availableCapacity} spaces remaining`}
                          </span>

                          <button
  className="button primary"
  onClick={() =>
    prepareBooking(modal, d)
  }
  disabled={
    d.status !== 'AVAILABLE' ||
    d.availableCapacity === 0
  }
>
  Book This Trip
</button>
                        </div>
                      )
                    )}

                    {bookingMessage && (
                      <p
                        className="notice"
                        role="status"
                      >
                        {
                          bookingMessage
                        }
                      </p>
                    )}

                    {bookingError && (
                      <p
                        className="error"
                        role="alert"
                      >
                        {
                          bookingError
                        }
                      </p>
                    )}
                  </div>
                ) : (
                  <p>
                    Departure dates
                    are coming soon.
                  </p>
                )}

                <p className="fine-print">
                  Currency is not
                  supplied by the
                  catalogue. Confirm
                  the currency before
                  booking. Images are
                  destination
                  inspiration.
                </p>
              </>
            )}
          </Modal>
        )}
    </>
  );
}

createRoot(
  document.getElementById(
    'root'
  )
).render(<App />);