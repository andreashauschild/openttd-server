// Session header. Hyphens only: nginx and other reverse proxies drop headers with underscores by default.
export const HEADER_OPENTTD_SERVER_SESSION_ID = "X-Openttd-Server-Session-Id"
// Header used up to the previous release. Only read from login responses so a rolling update keeps working.
export const LEGACY_HEADER_OPENTTD_SERVER_SESSION_ID = "X-OPENTTD_SERVER_SESSION_ID"
// localStorage key of the session id (used to be the header name itself)
export const SESSION_STORAGE_KEY = "openttd-server-session-id"

// Route parameters  = RP_<parameter name>
export const RP_ID = "id"
