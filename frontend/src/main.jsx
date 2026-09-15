import React, { useEffect, useRef, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { ArrowRight, CalendarDays, Check, Clock3, Compass, MapPin, Search, Send, Sparkles, Users, Wallet, X } from 'lucide-react';
import { getPackages, askAssistant } from './api';
import { photos, samplePackages, packageImage, matchingDepartures } from './data';
import './styles.css';

const blankFilters = { destination: '', date: '', travellers: '', budget: '', interests: '' };
const money = value => new Intl.NumberFormat('en-US', { maximumFractionDigits: 0 }).format(value);

function Modal({ title, children, close }) {
  const ref = useRef(null);
  useEffect(() => { const dialog = ref.current; dialog.showModal(); return () => dialog.close(); }, []);
  return <dialog ref={ref} onCancel={close} onClick={e => { if (e.target === ref.current) close(); }} aria-labelledby="dialog-title">
    <div className="modal-heading"><h2 id="dialog-title">{title}</h2><button className="icon-button" onClick={close} aria-label="Close dialog"><X size={21}/></button></div>{children}
  </dialog>;
}

function App() {
  const [packages, setPackages] = useState([]);
  const [state, setState] = useState('loading');
  const [filters, setFilters] = useState(blankFilters);
  const [applied, setApplied] = useState(null);
  const [showAll, setShowAll] = useState(false);
  const [modal, setModal] = useState(null);
  const [message, setMessage] = useState('');
  const [answer, setAnswer] = useState('');
  const [aiError, setAiError] = useState('');
  const [asking, setAsking] = useState(false);

  async function load() {
    setState('loading');
    try { const data = await getPackages(); setPackages(data); setState(data.length ? 'live' : 'empty'); }
    catch { setPackages([]); setState('offline'); }
  }
  useEffect(() => { load(); }, []);
  const demo = state === 'offline' || state === 'empty';
  const source = demo ? samplePackages : packages;
  const results = source.filter(item => {
    if (!applied) return true;
    if (!item.destination.toLowerCase().includes(applied.destination.trim().toLowerCase())) return false;
    if (item.sample) return !applied.date && !applied.travellers && (!applied.budget || item.price <= Number(applied.budget));
    return !(applied.date || applied.travellers || applied.budget) || matchingDepartures(item, applied).length > 0;
  });
  const visible = applied || showAll ? results : results.slice(0, 3);
  function update(event) { setFilters(current => ({ ...current, [event.target.name]: event.target.value })); }
  function search(event) { event.preventDefault(); setApplied({ ...filters }); document.getElementById('packages').scrollIntoView({ behavior: 'smooth', block: 'start' }); }
  function openAssistant(prefill = false) {
    if (prefill) setMessage(`Help me plan a trip${filters.destination ? ` to ${filters.destination}` : ''}${filters.date ? ` departing ${filters.date}` : ''}${filters.travellers ? ` for ${filters.travellers} travellers` : ''}${filters.budget ? ` with a budget of ${filters.budget} per person` : ''}${filters.interests ? `. My interests: ${filters.interests}` : ''}.`);
    setModal('ai');
  }
  async function submitAi(event) {
    event.preventDefault(); if (!message.trim() || asking) return;
    setAsking(true); setAiError(''); setAnswer('');
    try { const data = await askAssistant(message.trim()); setAnswer(data.response); }
    catch { setAiError('Your travel assistant is unavailable right now. Please try again in a moment.'); }
    finally { setAsking(false); }
  }

  return <>
    <a className="skip-link" href="#main">Skip to content</a>
    <header className="header"><a className="brand" href="./" aria-label="Voyage home">Voyage<span className="brand-dot">.</span></a>
      <nav aria-label="Main navigation"><a className="active" href="#main" aria-current="page">Explore</a><button onClick={() => setModal('bookings')}>My Bookings</button><button onClick={() => openAssistant()}>AI Assistant</button></nav>
      <button className="profile" onClick={() => setModal('profile')}>Profile<span className="avatar">V</span></button>
    </header>
    <main id="main">
      <section className="hero" aria-labelledby="hero-title"><img className="hero-photo" src={photos.hero} alt="Tropical coastline with turquoise water and lush island cliffs" fetchPriority="high"/><div className="hero-shade"/>
        <div className="hero-copy"><span className="eyebrow"><span/> A WORLD OF POSSIBILITIES</span><h1 id="hero-title">Find your next unforgettable trip.</h1><p>Discover curated destinations tailored just for you, powered by intelligent AI.</p></div>
        <span className="photo-caption"><MapPin size={13}/> Somewhere you’d rather be</span>
      </section>
      <form className="search-panel" onSubmit={search}>
        <div className="search-fields">
          <label>Destination<div className="input-wrap"><MapPin/><input name="destination" value={filters.destination} onChange={update} placeholder="Where to?"/></div></label>
          <label>Departure date<div className="input-wrap"><CalendarDays/><input aria-label="Departure date" name="date" type="date" value={filters.date} min={new Date().toLocaleDateString('en-CA')} onChange={update}/></div></label>
          <label>Travellers<div className="input-wrap"><Users/><select name="travellers" value={filters.travellers} onChange={update}><option value="">Add guests</option>{[1,2,3,4,5,6,7,8].map(n => <option key={n} value={n}>{n} {n === 1 ? 'traveller' : 'travellers'}</option>)}</select></div></label>
          <label>Budget per person<div className="input-wrap"><Wallet/><select name="budget" value={filters.budget} onChange={update}><option value="">Any budget</option><option value="1500">Up to 1,500</option><option value="2500">Up to 2,500</option><option value="5000">Up to 5,000</option></select></div></label>
        </div>
        <div className="search-bottom"><input className="interests" name="interests" aria-label="Interests for your AI request" value={filters.interests} onChange={update} placeholder="Add interests for AI (e.g. food, hiking)"/><div className="search-actions"><button type="button" className="button secondary" onClick={() => openAssistant(true)}><Sparkles size={16}/>Ask AI Instead</button><button className="button primary" type="submit"><Search size={16}/>Search Trips</button></div></div>
      </form>
      <section className="packages-section" id="packages" aria-labelledby="packages-title">
        <div className="section-heading"><div><span className="section-kicker">A LITTLE INSPIRATION</span><h2 id="packages-title">{applied ? 'Your next adventure' : showAll ? 'Explore all packages' : 'Featured Packages'}</h2><p>{applied ? `${results.length} ${results.length === 1 ? 'trip' : 'trips'} to explore. Your next story starts here.` : 'Extraordinary places. Thoughtfully chosen experiences.'}</p></div><button className="text-button" onClick={() => { setShowAll(!showAll); setApplied(null); }}>{showAll || applied ? 'Back to featured' : 'See all'}<ArrowRight size={16}/></button></div>
        {demo && <div className="notice" role="status">{state === 'offline' ? 'Showing sample trips while the catalogue is unavailable.' : 'Your catalogue is empty. Explore these sample trips for inspiration.'} <button onClick={load}>Retry catalogue</button></div>}
        {state === 'loading' ? <div className="cards" aria-label="Loading trips" aria-busy="true">{[1,2,3].map(n => <div className="skeleton" key={n}/>)}</div> : <div className="cards">{visible.map((item, index) => {
          const departures = matchingDepartures(item, applied || blankFilters).sort((a,b) => a.price - b.price);
          const first = departures[0]; const price = item.sample ? item.price : first?.price;
          const days = item.sample ? item.days : first ? Math.round((Date.parse(first.endDate) - Date.parse(first.startDate)) / 86400000) : null;
          return <article className="card" key={item.packageId}><div className="card-photo"><img src={packageImage(item)} alt={`Travel inspiration for ${item.destination}`} loading="lazy"/>{index === 0 && !applied && <span className="badge"><Sparkles size={12}/>{demo ? 'Top Pick' : 'Discover'}</span>}{item.sample && <span className="sample-badge">Sample trip</span>}</div><div className="card-body"><span className="destination">{item.destination}</span><h3>{item.name}</h3><p className="duration"><Clock3 size={14}/>{days != null ? `${days} Days` : 'Dates coming soon'}</p><div className="card-bottom"><div>{price != null ? <><strong>{item.sample ? '$' : ''}{money(price)}</strong><span> / person</span></> : <span>Price unavailable</span>}</div><button className="text-button" onClick={() => setModal(item)}>View Details<ArrowRight size={13}/></button></div></div></article>;
        })}</div>}
        {state !== 'loading' && !results.length && <div className="empty-state"><Compass size={32}/><h3>A different adventure awaits</h3><p>Try another destination or fewer filters.{demo && ' Sample trips do not have bookable dates or availability.'}</p><button className="button secondary" onClick={() => { setFilters(blankFilters); setApplied(null); }}>Clear filters</button></div>}
        <div className="section-footnote"><Check size={14}/><span>A little inspiration today. An unforgettable journey tomorrow.</span></div>
      </section>
    </main>
    <footer><a className="brand" href="./">Voyage<span className="brand-dot">.</span></a><span>Go somewhere that stays with you.</span><span>Made for the curious.</span></footer>
    <button className="floating-assistant" onClick={() => openAssistant()} aria-label="Open AI travel assistant"><Sparkles size={25}/></button>
    {modal === 'ai' && <Modal title="Your travel inspiration, unlocked." close={() => setModal(null)}><p className="modal-description">Tell Voyage what your ideal escape looks like. Our AI assistant will help you find a place to start.</p><form onSubmit={submitAi}><label className="ai-label" htmlFor="ai-message">What do you have in mind?</label><textarea id="ai-message" value={message} onChange={e => setMessage(e.target.value)} placeholder="A relaxing week in Bali with great food…" required maxLength={3000}/><button className="button primary" disabled={asking || !message.trim()}><Send size={16}/>{asking ? 'Finding inspiration…' : 'Ask Voyage'}</button></form><div aria-live="polite">{asking && <p>Thinking about your next adventure…</p>}{aiError && <p className="error">{aiError}</p>}{answer && <div className="ai-answer">{answer}</div>}</div><p className="fine-print">Suggestions are a starting point. Confirm prices, dates and availability in the package details.</p></Modal>}
    {(modal === 'profile' || modal === 'bookings') && <Modal title={modal === 'profile' ? 'Your Voyage profile' : 'Your journeys'} close={() => setModal(null)}><p className="modal-description">{modal === 'profile' ? 'Personal profiles are coming soon. You can already explore destinations and plan with the AI assistant.' : 'Personal booking history is coming soon. In the meantime, discover where your next journey could take you.'}</p><button className="button primary" onClick={() => setModal(null)}>Keep exploring<ArrowRight size={16}/></button></Modal>}
    {modal && typeof modal === 'object' && <Modal title={modal.name} close={() => setModal(null)}><img className="detail-image" src={packageImage(modal)} alt={modal.destination}/><p className="detail-location"><MapPin size={16}/>{modal.destination}</p><p className="modal-description">{modal.description}</p>{modal.sample ? <p className="notice">This is a sample itinerary for inspiration. Live departure dates and booking are not available.</p> : <><h3>Departures</h3>{modal.departureError ? <p>Departure details are temporarily unavailable.</p> : modal.departures.length ? <div className="departures">{modal.departures.map(d => <div className="departure" key={d.departureId}><strong>{d.startDate} → {d.endDate}</strong><span>{money(d.price)} per person · {d.status.toLowerCase().replaceAll('_', ' ')}</span><span>{d.availableCapacity == null ? 'Availability unconfirmed' : `${d.availableCapacity} spaces remaining`}</span></div>)}</div> : <p>Departure dates are coming soon.</p>}<p className="fine-print">Currency is not supplied by the catalogue. Confirm the currency before booking. Images are destination inspiration.</p></>}</Modal>}
  </>;
}

createRoot(document.getElementById('root')).render(<App/>);
