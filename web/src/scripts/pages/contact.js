/**
 * scripts/pages/contact.js
 *
 * Service-request form behaviour: validation rules, payload shaping, and the
 * submit lifecycle. The Svelte component imports these and stays limited to
 * rendering plus event binding.
 *
 * Stricter than the artist site's equivalent — a crew cannot quote a job
 * without a phone number and an address, so both are required here.
 */

import { api, ApiError } from '$lib/api/client.js';

export const SERVICE_TYPES = [
  { value: 'mowing',     label: 'Mowing and lawn care' },
  { value: 'cleanup',    label: 'Cleanup or bed reset' },
  { value: 'hardscape',  label: 'Patio, wall, or drainage' },
  { value: 'planting',   label: 'Planting and beds' },
  { value: 'irrigation', label: 'Irrigation' },
  { value: 'redesign',   label: 'Full redesign' },
  { value: 'other',      label: 'Something else' }
];

export const FREQUENCIES = [
  { value: 'weekly',   label: 'Weekly' },
  { value: 'biweekly', label: 'Every other week' },
  { value: 'monthly',  label: 'Monthly' },
  { value: 'one-time', label: 'One-time job' }
];

export const URGENCIES = [
  { value: 'asap',            label: 'As soon as possible' },
  { value: 'this-month',      label: 'Within a month' },
  { value: 'this-season',     label: 'Sometime this season' },
  { value: 'planning-ahead',  label: 'Planning ahead' }
];

export const PROPERTY_SIZES = [
  { value: 'under-quarter', label: 'Under 1/4 acre' },
  { value: 'quarter-half',  label: '1/4 to 1/2 acre' },
  { value: 'half-acre',     label: '1/2 to 1 acre' },
  { value: 'over-acre',     label: 'Over an acre' },
  { value: 'commercial',    label: 'Commercial property' }
];

/** Shape of a blank form. */
export function emptyForm() {
  return {
    name: '',
    email: '',
    phone: '',
    propertyAddress: '',
    message: '',
    serviceType: 'mowing',
    frequency: 'weekly',
    urgency: 'this-month',
    propertySize: 'quarter-half',
    website: '' // honeypot; stays empty for real people
  };
}

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

/** Counts digits so formatting characters don't affect the length check. */
function digitCount(value) {
  return (value.match(/\d/g) ?? []).length;
}

/**
 * Client-side validation, mirroring the server's bean-validation constraints.
 * The server remains the authority; this exists for immediate feedback.
 *
 * @returns {Record<string, string>} field name -> message, empty when valid
 */
export function validate(form) {
  const errors = {};

  if (!form.name.trim()) {
    errors.name = 'Enter your name';
  } else if (form.name.trim().length > 120) {
    errors.name = 'Name is too long';
  }

  if (!form.email.trim()) {
    errors.email = 'Enter your email address';
  } else if (!EMAIL_PATTERN.test(form.email.trim())) {
    errors.email = 'Enter a valid email address';
  }

  if (!form.phone.trim()) {
    errors.phone = 'Enter a phone number so the crew can reach you';
  } else if (digitCount(form.phone) < 10) {
    errors.phone = 'Enter a full phone number including area code';
  }

  if (!form.propertyAddress.trim()) {
    errors.propertyAddress = 'Enter the address of the property';
  } else if (form.propertyAddress.trim().length > 300) {
    errors.propertyAddress = 'Address is too long';
  }

  if (!form.message.trim()) {
    errors.message = 'Tell us what the property needs';
  } else if (form.message.trim().length > 4000) {
    errors.message = 'Message is too long';
  }

  return errors;
}

/**
 * Submit the form.
 *
 * Returns a discriminated result rather than throwing, so the component can
 * render every outcome without a try/catch in the template layer.
 *
 * @returns {Promise<{ok: true, message: string} | {ok: false, message: string, fields: Record<string,string>}>}
 */
export async function submitForm(form) {
  const errors = validate(form);
  if (Object.keys(errors).length > 0) {
    return {
      ok: false,
      message: 'Check the highlighted fields and try again.',
      fields: errors
    };
  }

  try {
    const response = await api.submitInquiry({
      name: form.name.trim(),
      email: form.email.trim(),
      phone: form.phone.trim(),
      propertyAddress: form.propertyAddress.trim(),
      message: form.message.trim(),
      serviceType: form.serviceType,
      frequency: form.frequency,
      urgency: form.urgency,
      propertySize: form.propertySize,
      website: form.website
    });

    return {
      ok: true,
      message: response?.message ?? 'Thanks — your request is in.'
    };
  } catch (error) {
    if (error instanceof ApiError) {
      return {
        ok: false,
        message: error.message,
        fields: error.body?.fields ?? {}
      };
    }
    return {
      ok: false,
      message: 'Something went wrong. Try again in a moment.',
      fields: {}
    };
  }
}
