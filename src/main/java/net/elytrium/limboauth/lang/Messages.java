/*
 * Copyright (C) 2021 - 2024 Elytrium
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.elytrium.limboauth.lang;

public class Messages {

  // System Messages
  public String reload = "{PRFX} &aSuccessfully reloaded!";
  public String errorOccurred = "{PRFX} &cAn internal error occurred!";
  public String ratelimited = "{PRFX} &cPlease wait before using this again!";
  public String databaseErrorKick = "{PRFX} &cA database error occurred!";

  // Player Messages
  public String notPlayer = "{PRFX} &cConsole cannot execute this command!";
  public String notRegistered = "{PRFX} &cYou are not registered or your account is &6PREMIUM&c!";
  public String crackedCommand = "{PRFX}{NL}&aYou cannot use this command because your account is &6PREMIUM&a!";
  public String wrongPassword = "{PRFX} &cWrong password!";

  // Kick Messages
  public String nicknameInvalidKick = "{PRFX}{NL}&cYour username contains prohibited characters. Please change your username!";
  public String reconnectKick = "{PRFX}{NL}&cReconnect to the server to verify your account!";
  public String ipLimitKick = "{PRFX}{NL}{NL}&cYour IP has reached the maximum number of registered accounts. If this is a mistake, restart your router or wait about 6 hours.";
  public String wrongNicknameCaseKick = "{PRFX}{NL}&cYou must log in with username &6{0}&c, not &6{1}&c.";
  public String registrationsDisabledKick = "{PRFX}{NL}&cRegistrations are currently disabled.";

  // Boss Bar & Timeout
  public String bossbar = "&7You have &b{0} &7seconds left to log in.";
  public String timesUp = "{PRFX} &cYour time is up, please reconnect.";

  // Premium Login
  public String loginPremium = "{PRFX} Automatically logged in with premium account!";
  public String loginPremiumTitle = "{PRFX} Welcome!";
  public String loginPremiumSubtitle = "&aLogged in as a premium player!";

  // Floodgate Login
  public String loginFloodgate = "{PRFX} Automatically logged in with Bedrock account!";
  public String loginFloodgateTitle = "{PRFX} Welcome!";
  public String loginFloodgateSubtitle = "&aLogged in as a Bedrock player!";

  // Login
  public String login = "{PRFX} &b/login <password>&7 to log in.";
  public String loginWrongPassword = "{PRFX} &cWrong password, you have &6{0} &cattempts left.";
  public String loginWrongPasswordKick = "{PRFX}{NL}&cYou entered the wrong password too many times!";
  public String loginSuccessful = "{PRFX} &aSuccessfully logged in!";
  public String loginTitle = "&b&l[CRAFTER]";
  public String loginSubtitle = "&7Please log in, &b{0} &7attempts left.";
  public String loginSuccessfulTitle = "&a&l✔";
  public String loginSuccessfulSubtitle = "&aSuccessfully logged in!";

  // Register
  public String register = "{PRFX} &b/register <password> <repeat password> &7to register";
  public String registerDifferentPasswords = "{PRFX} &cThe passwords you entered are different!";
  public String registerPasswordTooShort = "{PRFX} &cThe password you entered is too short, use a different password!";
  public String registerPasswordTooLong = "{PRFX} &cThe password you entered is too long, use a different password!";
  public String registerPasswordUnsafe = "{PRFX} &cYour password is not secure enough. It should contain uppercase and lowercase letters and numbers. Do not use your username in your password!";
  public String registerSuccessful = "{PRFX} &aSuccessfully registered!";
  public String registerTitle = "&b&l[CRAFTER]";
  public String registerSubtitle = "&7Please register using &b/register";
  public String registerSuccessfulTitle = "&a&l✔";
  public String registerSuccessfulSubtitle = "&aSuccessfully registered!";
  public String registerEnterEmail = "{PRFX} &aPlease enter your email address:";
  public String registerInvalidEmail = "{PRFX} &cInvalid email address format!";

  // TOTP (2FA)
  public String totp = "{PRFX} &b/2fa <code>&7 to verify.";
  public String totpTitle = "&b&l[CRAFTER]";
  public String totpSubtitle = "&7Please enter your 2FA code.";
  public String totpSuccessful = "{PRFX} &aSuccessfully verified with 2FA!";
  public String totpSuccessfulTitle = "&a&l✔";
  public String totpSuccessfulSubtitle = "&a2FA verification successful!";
  public String totpDisabled = "{PRFX} &a2FA is now disabled!";
  public String totpEnabled = "{PRFX} &a2FA is now enabled!";
  public String totpEnabledAlready = "{PRFX} &c2FA is already enabled!";
  public String totpDisabledAlready = "{PRFX} &c2FA is already disabled!";
  public String totpLink = "{PRFX} &aClick the link to set up 2FA: &6{0}";
  public String totpToken = "{PRFX} &aOr manually enter this key: &6{0}";
  public String totpQr = "{PRFX} &aOr scan this QR code: &6{0}";
  public String totpRecovery = "{PRFX} &aRecovery codes: &6{0}";
  public String totpWrong = "{PRFX} &cWrong 2FA code!";
  public String totpWrongKick = "{PRFX}{NL}&cYou entered the wrong 2FA code too many times!";
  public String totpUsage = "{PRFX} &cUsage: /2fa <enable|disable> [password|code]";

  // Password Commands
  public String destroySessionSuccessful = "{PRFX} &aYour session has been destroyed!";
  public String changePasswordSuccessful = "{PRFX} &aPassword successfully changed!";
  public String changePasswordMessage = "{PRFX} &6Your password has been successfully changed!";
  public String changePasswordUsage = "{PRFX} &cUsage: /changepassword <old password> <new password>";
  public String modSessionExpired = "{PRFX} &cYour session has expired. Please log in again.";

  // Events
  public String eventCancelled = "{PRFX} &cAuthorization event cancelled";

  // Unregister
  public String unregisterSuccessful = "{PRFX} &aAccount successfully deleted!";
  public String unregisterUsage = "{PRFX} &cUsage: /unregister <current password> confirm";

  // Premium Command
  public String premiumSuccessful = "{PRFX} &aNow logging in as premium user!";
  public String notPremium = "{PRFX} &cYou are not a premium user!";
  public String alreadyPremium = "{PRFX} &cYou are already a premium user!";
  public String premiumDisabled = "{PRFX} &cThis feature is disabled by the administrator!";
  public String premiumUsage = "{PRFX} &cUsage: /premium <password> confirm";

  // Force Commands
  public String forceUnregisterSuccessful = "{PRFX} &a{0}'s account has been deleted!";
  public String forceUnregisterNotSuccessful = "{PRFX} &cUser is not registered!";
  public String forceUnregisterUsage = "{PRFX} &cUsage: /forceunregister <username>";
  public String forceUnregisterKick = "{PRFX}{NL}&cYour account has been deleted by an administrator!";
  public String forceRegisterSuccessful = "{PRFX} &a{0}'s account has been created!";
  public String forceRegisterNotSuccessful = "{PRFX} &cUser is already registered!";
  public String forceRegisterUsage = "{PRFX} &cUsage: /forceregister <username> <password>";
  public String forceRegisterTakenNickname = "{PRFX} &cThis nickname is already taken!";
  public String forceRegisterIncorrectNickname = "{PRFX} &cThis nickname contains invalid characters!";
  public String forceLoginSuccessful = "{PRFX} &a{0} has been logged in!";
  public String forceLoginUnknownPlayer = "{PRFX} &c{0} is not online or not in the authentication queue!";
  public String forceLoginUsage = "{PRFX} &cUsage: /forcelogin <username>";
  public String forceChangepasswordSuccessful = "{PRFX} &a{0}'s password has been changed!";
  public String forceChangepasswordNotSuccessful = "{PRFX} &cUser is not registered!";
  public String forceChangepasswordNotRegistered = "{PRFX} &c{0} is not registered!";
  public String forceChangepasswordUsage = "{PRFX} &cUsage: /forcechangepassword <username> <new password>";

  // Discord 2FA
  public String discordCheckDm = "{PRFX} &e&lCheck your Discord DM!";
  public String discordVerifyPrompt = "{PRFX} &7Please respond to Discord verification";
  public String discordTimeout = "{PRFX} &760 seconds to verify or you will be kicked";
  public String discordApproved = "{PRFX} &aDiscord verification approved! Welcome to the server.";
  public String discordDenied = "{PRFX} &cDiscord verification was denied. Connection closed.";
  public String discordTimeoutKick = "{PRFX}{NL}&cDiscord verification timed out.";
  public String discordBossbar = "&eTime remaining for Discord verification: &b{0} &eseconds";

  // Staff Commands
  public String staffAdded = "{PRFX} &a{0} has been added to staff list with Discord ID: {1}";
  public String staffRemoved = "{PRFX} &a{0} has been removed from staff list!";
  public String staffAlreadyExists = "{PRFX} &c{0} is already in the staff list!";
  public String staffNotFound = "{PRFX} &c{0} is not in the staff list!";
  public String staffListHeader = "{PRFX} &e&lStaff List:";
  public String staffListEntry = "&7- &b{0} &7(Discord: &a{1}&7)";
  public String staffListEmpty = "{PRFX} &cNo staff members found!";
  public String staffReloaded = "{PRFX} &aStaff database reloaded!";
  public String staffUsage = "{PRFX} &cUsage: /crafterauth staff <add|remove|list|reload>";
  public String staffAddUsage = "{PRFX} &cUsage: /crafterauth staff add <username> <discord_id>";
  public String staffRemoveUsage = "{PRFX} &cUsage: /crafterauth staff remove <username>";

  // Discord Embed Messages
  public String discordEmbedTitle = "CrafterAuth Staff Login";
  public String discordEmbedDescription = "**{username}** is trying to log in to the server.";
  public String discordEmbedUsernameField = "Username";
  public String discordEmbedIpField = "IP Address";
  public String discordEmbedTimeField = "Time";
  public String discordButtonApprove = "Approve";
  public String discordButtonDeny = "Deny";
  public String discordTimeoutMessage = "Login timeout";
  public String discordApprovedMessage = "Login verified!";
  public String discordDeniedMessage = "Login denied";
  public String discordIpMismatchMessage = "IP address mismatch";
  public String discord2faMessage = "CrafterAuth 2FA code: {code}";
}
