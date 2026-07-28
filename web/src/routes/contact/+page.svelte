<script>
  import { fly, fade } from 'svelte/transition';
  import {
    emptyForm,
    submitForm,
    SERVICE_TYPES,
    FREQUENCIES,
    URGENCIES,
    PROPERTY_SIZES
  } from '$scripts/pages/contact.js';
  import './page.css';

  let form = $state(emptyForm());
  let errors = $state({});
  let submitting = $state(false);
  let result = $state(null); // { ok, message }

  /** Recurring service needs a frequency; a one-off job does not. */
  const showsFrequency = $derived(
    form.serviceType === 'mowing' || form.serviceType === 'cleanup'
  );

  async function handleSubmit(event) {
    event.preventDefault();
    submitting = true;
    result = null;

    const outcome = await submitForm(form);

    if (outcome.ok) {
      result = { ok: true, message: outcome.message };
      errors = {};
      form = emptyForm();
    } else {
      result = { ok: false, message: outcome.message };
      errors = outcome.fields ?? {};
    }

    submitting = false;
  }

  /** Clear a field's error as soon as the person starts correcting it. */
  function clearError(field) {
    if (errors[field]) {
      const { [field]: _, ...rest } = errors;
      errors = rest;
    }
  }
</script>

<svelte:head>
  <title>Get a Quote — Exceptional Landscaping &amp; Lawn Services</title>
  <meta name="description" content="Request lawn service or a landscaping estimate. We walk the property and send a written, fixed-price scope." />
</svelte:head>

  <div class="quote">
    <section class="cards shell" aria-label="Our card">
      <article class="cards__row cards__row--front">
        <figure class="cards__figure">
          <img
            class="cards__img"
            src="/site/contact-card-front.png"
            alt="Exceptional Landscaping business card, front"
            loading="lazy"
            decoding="async"
          />
        </figure>
        <div class="cards__blurb">
          <span class="eyebrow">Leave one on the fridge</span>
          <h2 class="cards__blurb-title">Keep us handy</h2>
          <p class="cards__blurb-text">
            Grab a card next time we are on site, or save the number below.
            One text and we will get you on the schedule.
          </p>
        </div>
      </article>

      <article class="cards__row cards__row--back">
        <div class="cards__blurb">
          <span class="eyebrow">What is on the back</span>
          <h2 class="cards__blurb-title">Everything we do</h2>
          <p class="cards__blurb-text">
            Mowing through full redesigns — the back lists the services and the
            areas we cover across greater Louisville.
          </p>
        </div>
        <figure class="cards__figure">
          <img
            class="cards__img"
            src="/site/contact-card-back.png"
            alt="Exceptional Landscaping business card, back"
            loading="lazy"
            decoding="async"
          />
        </figure>
      </article>
    </section>

    <header class="quote__head shell">
      <span class="eyebrow">Free estimate</span>
      <h1 class="quote__title">Request lawn service now</h1>
      <p class="quote__lede">
        Tell us the address and what the property needs. We walk it ourselves,
        usually within a week, and send a written fixed price.
      </p>
    </header>

  <div class="quote__body shell">
    <form class="form" onsubmit={handleSubmit} novalidate>
      {#if result}
        <div
          class="form__banner"
          class:form__banner--ok={result.ok}
          class:form__banner--bad={!result.ok}
          role="status"
          in:fly={{ y: -8, duration: 280 }}
        >
          {result.message}
        </div>
      {/if}

      <fieldset class="fieldset">
        <legend class="fieldset__legend">How to reach you</legend>

        <div class="form__row">
          <div class="field">
            <label class="field__label" for="name">Your name</label>
            <input
              id="name"
              class="field__input"
              class:field__input--bad={errors.name}
              type="text"
              bind:value={form.name}
              oninput={() => clearError('name')}
              aria-invalid={!!errors.name}
              aria-describedby={errors.name ? 'name-error' : undefined}
            />
            {#if errors.name}
              <p class="field__error" id="name-error" in:fade={{ duration: 150 }}>{errors.name}</p>
            {/if}
          </div>

          <div class="field">
            <label class="field__label" for="phone">Phone</label>
            <input
              id="phone"
              class="field__input"
              class:field__input--bad={errors.phone}
              type="tel"
              bind:value={form.phone}
              oninput={() => clearError('phone')}
              aria-invalid={!!errors.phone}
              aria-describedby={errors.phone ? 'phone-error' : undefined}
            />
            {#if errors.phone}
              <p class="field__error" id="phone-error" in:fade={{ duration: 150 }}>{errors.phone}</p>
            {/if}
          </div>
        </div>

        <div class="field">
          <label class="field__label" for="email">Email</label>
          <input
            id="email"
            class="field__input"
            class:field__input--bad={errors.email}
            type="email"
            bind:value={form.email}
            oninput={() => clearError('email')}
            aria-invalid={!!errors.email}
            aria-describedby={errors.email ? 'email-error' : undefined}
          />
          {#if errors.email}
            <p class="field__error" id="email-error" in:fade={{ duration: 150 }}>{errors.email}</p>
          {/if}
        </div>
      </fieldset>

      <fieldset class="fieldset">
        <legend class="fieldset__legend">The property</legend>

        <div class="field">
          <label class="field__label" for="propertyAddress">Property address</label>
          <input
            id="propertyAddress"
            class="field__input"
            class:field__input--bad={errors.propertyAddress}
            type="text"
            autocomplete="street-address"
            bind:value={form.propertyAddress}
            oninput={() => clearError('propertyAddress')}
            aria-invalid={!!errors.propertyAddress}
            aria-describedby={errors.propertyAddress ? 'address-error' : undefined}
          />
          {#if errors.propertyAddress}
            <p class="field__error" id="address-error" in:fade={{ duration: 150 }}>
              {errors.propertyAddress}
            </p>
          {/if}
        </div>

        <div class="form__row">
          <div class="field">
            <label class="field__label" for="propertySize">Property size</label>
            <select id="propertySize" class="field__input" bind:value={form.propertySize}>
              {#each PROPERTY_SIZES as option}
                <option value={option.value}>{option.label}</option>
              {/each}
            </select>
          </div>

          <div class="field">
            <label class="field__label" for="serviceType">What do you need</label>
            <select id="serviceType" class="field__input" bind:value={form.serviceType}>
              {#each SERVICE_TYPES as option}
                <option value={option.value}>{option.label}</option>
              {/each}
            </select>
          </div>
        </div>

        <div class="form__row">
          {#if showsFrequency}
            <div class="field" in:fade={{ duration: 180 }}>
              <label class="field__label" for="frequency">How often</label>
              <select id="frequency" class="field__input" bind:value={form.frequency}>
                {#each FREQUENCIES as option}
                  <option value={option.value}>{option.label}</option>
                {/each}
              </select>
            </div>
          {/if}

          <div class="field">
            <label class="field__label" for="urgency">When</label>
            <select id="urgency" class="field__input" bind:value={form.urgency}>
              {#each URGENCIES as option}
                <option value={option.value}>{option.label}</option>
              {/each}
            </select>
          </div>
        </div>

        <div class="field">
          <label class="field__label" for="message">What does the property need</label>
          <textarea
            id="message"
            class="field__input field__input--area"
            class:field__input--bad={errors.message}
            rows="6"
            bind:value={form.message}
            oninput={() => clearError('message')}
            aria-invalid={!!errors.message}
            aria-describedby={errors.message ? 'message-error' : undefined}
          ></textarea>
          {#if errors.message}
            <p class="field__error" id="message-error" in:fade={{ duration: 150 }}>{errors.message}</p>
          {/if}
        </div>
      </fieldset>

      <!-- Honeypot: hidden from people, irresistible to bots. -->
      <div class="form__trap" aria-hidden="true">
        <label for="website">Website</label>
        <input id="website" type="text" tabindex="-1" autocomplete="off" bind:value={form.website} />
      </div>

      <button class="btn btn--solid form__submit" type="submit" disabled={submitting}>
        {submitting ? 'Sending…' : 'Request service'}
      </button>
    </form>

    <aside class="aside">
      <h2 class="aside__title">What happens next</h2>
      <ol class="aside__steps">
        <li class="aside__step">
          <span class="aside__step-label">First</span>
          A call within one business day to confirm details.
        </li>
        <li class="aside__step">
          <span class="aside__step-label">Then</span>
          We walk the property and take our own measurements.
        </li>
        <li class="aside__step">
          <span class="aside__step-label">Then</span>
          A written scope with a fixed price, not a range.
        </li>
        <li class="aside__step">
          <span class="aside__step-label">Finally</span>
          Scheduling, usually two to four weeks out in season.
        </li>
      </ol>

      <div class="aside__urgent">
        <span class="aside__urgent-label">Need someone today</span>
        <a href="tel:+15025550142" class="aside__phone">(502) 555-0142</a>
        <p class="aside__hours">Monday to Friday, 7am to 5pm</p>
      </div>
    </aside>
  </div>
</div>
