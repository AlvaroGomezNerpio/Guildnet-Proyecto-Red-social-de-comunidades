/** Icono de Bootstrap Icons según el sitio al que apunta un enlace. */
const ICONS: readonly [RegExp, string][] = [
  [/(^|\.)discord\.(gg|com)$/, 'discord'],
  [/(^|\.)(youtube\.com|youtu\.be)$/, 'youtube'],
  [/(^|\.)twitch\.tv$/, 'twitch'],
  [/(^|\.)(twitter\.com|x\.com)$/, 'twitter-x'],
  [/(^|\.)instagram\.com$/, 'instagram'],
  [/(^|\.)tiktok\.com$/, 'tiktok'],
  [/(^|\.)github\.com$/, 'github'],
  [/(^|\.)reddit\.com$/, 'reddit'],
  [/(^|\.)(facebook\.com|fb\.com)$/, 'facebook'],
  [/(^|\.)telegram\.(me|org)$|(^|\.)t\.me$/, 'telegram'],
  [/(^|\.)(whatsapp\.com|wa\.me)$/, 'whatsapp'],
  [/(^|\.)spotify\.com$/, 'spotify'],
  [/(^|\.)steamcommunity\.com$|(^|\.)steampowered\.com$/, 'steam'],
  [/(^|\.)mastodon\./, 'mastodon'],
  [/(^|\.)linkedin\.com$/, 'linkedin'],
];

export function linkIcon(url: string): string {
  try {
    const host = new URL(url).hostname.replace(/^www\./, '').toLowerCase();
    return ICONS.find(([pattern]) => pattern.test(host))?.[1] ?? 'globe2';
  } catch {
    return 'link-45deg';
  }
}

/** Dominio legible de un enlace (sin "www."). */
export function linkHost(url: string): string {
  try {
    return new URL(url).hostname.replace(/^www\./, '');
  } catch {
    return url;
  }
}
