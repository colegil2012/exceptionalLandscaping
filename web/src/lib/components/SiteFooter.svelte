<script>
  import './SiteFooter.css';
  const year = new Date().getFullYear();
</script>

<footer class="footer">
  <div class="footer__inner shell">
    <div class="footer__brand">
      <span class="footer__name">Ridgeline</span>
      <p class="footer__line">
        Lawn care, hardscape, and full redesigns across greater Louisville.
        Licensed and insured.
      </p>
      <a href="tel:+15025550142" class="footer__phone">(502) 555-0142</a>
    </div>

    <nav class="footer__col" aria-label="Footer">
      <span class="footer__coltitle">Pages</span>
      <a href="/gallery" class="footer__link">Our Work</a>
      <a href="/bio" class="footer__link">About</a>
      <a href="/contact" class="footer__link">Get a Quote</a>
    </nav>

    <div class="footer__col">
      <span class="footer__coltitle">Service Area</span>
      <span class="footer__item">Louisville</span>
      <span class="footer__item">St. Matthews</span>
      <span class="footer__item">Prospect</span>
      <span class="footer__item">Jeffersontown</span>
    </div>
  </div>

  <div class="footer__base shell">
    <span>&copy; {year} Ridgeline Lawn &amp; Landscape</span>
    <a href="/contact" class="footer__cta">Request lawn service</a>
  </div>
</footer>
