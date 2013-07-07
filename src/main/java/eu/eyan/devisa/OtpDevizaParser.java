package eu.eyan.devisa;

import static eu.eyan.devisa.OtpDevizaParser.Valuta.*;
import static eu.eyan.devisa.OtpDevizaParser.ÉrtékTípus.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;

import javax.mail.MessagingException;

import org.joda.time.DateTime;

import eu.eyan.mail.SendMailTLS;

public class OtpDevizaParser {

	private static final SimpleDateFormat DÁTUM_FORMÁTUM_NAP = new SimpleDateFormat("MMMM dd", new Locale("hu"));
	private static final SimpleDateFormat DÁTUM_FORMÁTUM_PERC = new SimpleDateFormat("yyyy.MMMM.dd HH:mm", new Locale("hu"));
	private static final String FT = " Ft";
	private static final String BR = System.lineSeparator();

	public enum ÉrtékTípus {
		Egység, Közép, Valuta_vételi, Valuta_eladási, Csekk_vételi, Csekk_eladási, Deviza_vételi, Deviza_eladási;
	}

	public enum Valuta {
		AUD, BGN, CAD, CHF, CNY, CZK, DKK, EUR, GBP, HRK, JPY, NOK, PLN, RON, RSD, RUB, SEK, TRY, UAH, USD
	}

	public static final String PAGE = "https://www.otpbank.hu/portal/hu/Arfolyamok/OTP";
	private static String otpDevizaOldalHtml = null;
	private static DateTime utoljáraBeolvasva;

	public static float getValue(Valuta valuta, ÉrtékTípus értékTípus) {
		String tábla = getTáblaHtml(getOtpDevizaOldalHtml());
		String devizaAdatok = getDevizaAdatokHtml(tábla, valuta);
		devizaAdatok = devizaAdatok.replaceAll("\\s", "").replaceAll("<tdclass=\"num\">", "");
		String[] adatok = devizaAdatok.split("</td>");
		String adatSzöveg = adatok[értékTípus.ordinal()];
		return Float.parseFloat(adatSzöveg.replace(",", "."));
	}

	private static String getDevizaAdatokHtml(String tábla, Valuta valuta) {
		String adatokElejeKeresőkifejezés = valuta+"</b></td>";
		int adatokEleje = tábla.indexOf(adatokElejeKeresőkifejezés) + adatokElejeKeresőkifejezés.length();
		String adatokDevizaElejétől = tábla.substring(adatokEleje);
		int adatokVége = adatokDevizaElejétől.indexOf("</tr");
		String devizaAdatok = adatokDevizaElejétől.substring(0, adatokVége);
		return devizaAdatok;
	}

	private static String getTáblaHtml(String page) {
		int táblaEleje = page.indexOf("<table cellspacing=\"0\" cellpadding=\"0\" summary=\"\" class=\"appdata\" style=\"width: 721px; margin-left: 0;\">");
		String táblától = page.substring(táblaEleje);
		int táblaVége = táblától.indexOf("</table>");
		String tábla = táblától.substring(0, táblaVége);
		return tábla;
	}

	public static String getOtpDevizaOldalHtml() {
		if(otpDevizaOldalHtml == null || utoljáraBeolvasva.plusHours(1).isBeforeNow()){
			try {
				Scanner scanner = new Scanner(new URL(PAGE).openStream(), "UTF-8");
				try {
					otpDevizaOldalHtml = scanner.useDelimiter("\\A").next();
					utoljáraBeolvasva = new DateTime();
				} finally {
					scanner.close();
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return otpDevizaOldalHtml;
	}
	
	public static void main(String args[])
	{
		final String username = System.getenv("GMAIL_USER");
		final String password = System.getenv("GMAIL_PASS");
		final String from = System.getenv("GMAIL_FROM");
		final String to = System.getenv("GMAIL_TO");
		String subject = EUR + " " + getValue(EUR, Közép) + FT + " - " + DÁTUM_FORMÁTUM_NAP.format(new Date());
		String body    = new StringBuilder("Otp " + EUR + "" + BR + BR)
								   .append("Közép:          " + getValue(EUR, Közép) + FT + BR + BR)
								   .append("Deviza_vételi:  " + getValue(EUR, Deviza_vételi) + FT + BR + BR)
								   .append("Deviza_eladási: " + getValue(EUR, Deviza_eladási) + FT + BR + BR)
								   .append(BR + BR)
								   .append(DÁTUM_FORMÁTUM_PERC.format(new Date()))
								   .toString();
		try {
			System.out.println("LOG: send email: " + username + BR + "  " + password + BR + "  " + from + BR + "  " + to + BR + "  " + subject + BR + "  " + body);
			SendMailTLS.send(username, password, from, to, subject, body);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	
	
}
