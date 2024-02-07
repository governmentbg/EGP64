package com.ib.docu.utils;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import javax.xml.datatype.DatatypeConfigurationException;

import com.ib.docu.db.dto.Referent;
import com.ib.docu.db.dto.ReferentAddress;
import com.ib.docu.system.DocuConstants;
import com.ib.docu.system.SystemData;
import com.ib.indexui.system.Constants;
import com.ib.system.BaseSystemData;
import com.ib.system.SysConstants;
import com.ib.system.db.dto.SystemClassif;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.utils.DateUtils;
import com.ib.system.utils.SearchUtils;

import bg.government.regixclient.RegixClient;
import bg.government.regixclient.RegixClientException;
import bg.government.regixclient.requests.av.tr.ActualStateRequestType;
import bg.government.regixclient.requests.av.tr.ActualStateResponseType;
import bg.government.regixclient.requests.av.tr.AddressType;
import bg.government.regixclient.requests.av.tr.DetailType;
import bg.government.regixclient.requests.av.tr.TROperation;
import bg.government.regixclient.requests.grao.GraoOperation;
import bg.government.regixclient.requests.grao.nbd.PersonDataRequestType;
import bg.government.regixclient.requests.grao.nbd.PersonDataResponseType;
import bg.government.regixclient.requests.grao.nbd.PersonNames;
import bg.government.regixclient.requests.grao.pna.PermanentAddressRequestType;
import bg.government.regixclient.requests.grao.pna.PermanentAddressResponseType;
import bg.government.regixclient.requests.grao.pna.TemporaryAddressRequestType;
import bg.government.regixclient.requests.grao.pna.TemporaryAddressResponseType;

/**
 * Тука ще има методи, които от респонса на регикс сетват данни в нашите обекти фзл/нфл/адреси и т.н.
 *
 * @author belev
 */
public class RegixUtils {

	/**
	 * Сетва в обекта ReferentAddress данните от RegIX за постоянен адрес.
	 *
	 * @param address
	 * @param response
	 * @param sd
	 * @return <code>true</code> ако има разлики от RegIX спрямо обекта, иначе <code>false</code>
	 * @throws DbErrorException
	 */
	public static boolean setFzlReferentAddressData(ReferentAddress address, PermanentAddressResponseType response, BaseSystemData sd) throws DbErrorException {
		if (response == null) {
			return false;
		}
		address.setAddrType(DocuConstants.CODE_ZNACHENIE_ADDR_TYPE_POSTOQNEN); // това е безусловно защото за нов запис трябва, а
																				// за корекция е еднакво
		address.setAddrCountry(37); // дали винаги е БД

		boolean changed = false;

		//
		StringBuilder addrText = new StringBuilder();
		String t = SearchUtils.trimToNULL(response.getCityArea());
		if (t != null) {
			if (addrText.length() > 0) {
				addrText.append(", ");
			}
			addrText.append(t);
		}
		t = SearchUtils.trimToNULL(response.getLocationName());
		if (t != null) {
			if (addrText.length() > 0) {
				addrText.append(", ");
			}
			String tUp = t.toUpperCase();
			if (tUp.indexOf("БУЛ") == -1 && tUp.indexOf("УЛ") == -1 && tUp.indexOf("Ж.К") == -1 && tUp.indexOf("ЖК") == -1 && tUp.indexOf("ПЛ") == -1) {
				addrText.append("ул. "); // ако няма изрично булевар или улица добавям улица
			}
			addrText.append(t);
		}
		t = SearchUtils.trimToNULL(response.getBuildingNumber());
		if (t != null) {
			if (addrText.length() > 0) {
				addrText.append(" ");
			}
			addrText.append("№ " + t);
		}
		t = SearchUtils.trimToNULL(response.getEntrance());
		if (t != null) {
			if (addrText.length() > 0) {
				addrText.append(", ");
			}
			addrText.append("вх. " + t);
		}
		t = SearchUtils.trimToNULL(response.getFloor());
		if (t != null) {
			if (addrText.length() > 0) {
				addrText.append(", ");
			}
			addrText.append("ет. " + t);
		}
		t = SearchUtils.trimToNULL(response.getApartment());
		if (t != null) {
			if (addrText.length() > 0) {
				addrText.append(", ");
			}
			addrText.append("ап. " + t);
		}
		if (!isStringEq(address.getAddrText(), addrText.toString())) {
			changed = true;
			address.setAddrText(addrText.toString());
		}
		
		//
		Integer ekatte = null;
		String ekatteCode = SearchUtils.trimToNULL(response.getSettlementCode());
		if (ekatteCode != null) {
			try {
				ekatte = Integer.parseInt(ekatteCode);
			} catch (Exception e) { // при нас е число
			}
		}		
		if (ekatte != null && !Objects.equals(address.getEkatte(), ekatte)) {
			changed = true;
			address.setEkatte(ekatte);
		}

		return changed;
	}

	/**
	 * Сетва в обекта ReferentAddress данните от RegIX за настоящ адрес.
	 *
	 * @param address
	 * @param response
	 * @param sd
	 * @return <code>true</code> ако има разлики от RegIX спрямо обекта, иначе <code>false</code>
	 * @throws DbErrorException
	 */
	public static boolean setFzlReferentAddressData(ReferentAddress address, TemporaryAddressResponseType response, BaseSystemData sd) throws DbErrorException {
		if (response == null) {
			return false;
		}
		address.setAddrType(DocuConstants.CODE_ZNACHENIE_ADDR_TYPE_CORRESP); // това е безусловно защото за нов запис трябва, а за
																				// корекция е еднакво
		boolean changed = false;

		//
		StringBuilder addrText = new StringBuilder();
		String t = SearchUtils.trimToNULL(response.getCityArea());
		if (t != null) {
			if (addrText.length() > 0) {
				addrText.append(", ");
			}
			addrText.append(t);
		}
		t = SearchUtils.trimToNULL(response.getLocationName());
		if (t != null) {
			if (addrText.length() > 0) {
				addrText.append(", ");
			}
			String tUp = t.toUpperCase();
			if (tUp.indexOf("БУЛ") == -1 && tUp.indexOf("УЛ") == -1 && tUp.indexOf("Ж.К") == -1 && tUp.indexOf("ЖК") == -1 && tUp.indexOf("ПЛ") == -1) {
				addrText.append("ул. "); // ако няма изрично булевар или улица добавям улица
			}
			addrText.append(t);
		}
		t = SearchUtils.trimToNULL(response.getBuildingNumber());
		if (t != null) {
			if (addrText.length() > 0) {
				addrText.append(" ");
			}
			addrText.append("№ " + t);
		}
		t = SearchUtils.trimToNULL(response.getEntrance());
		if (t != null) {
			if (addrText.length() > 0) {
				addrText.append(", ");
			}
			addrText.append("вх. " + t);
		}
		t = SearchUtils.trimToNULL(response.getFloor());
		if (t != null) {
			if (addrText.length() > 0) {
				addrText.append(", ");
			}
			addrText.append("ет. " + t);
		}
		t = SearchUtils.trimToNULL(response.getApartment());
		if (t != null) {
			if (addrText.length() > 0) {
				addrText.append(", ");
			}
			addrText.append("ап. " + t);
		}
		if (!isStringEq(address.getAddrText(), addrText.toString())) {
			changed = true;
			address.setAddrText(addrText.toString());
		}

		//
		Integer addrCountry = null;
		Integer ekatte = null;
		String ekatteCode = SearchUtils.trimToNULL(response.getSettlementCode());
		if (ekatteCode != null) {
			try {
				ekatte = Integer.parseInt(ekatteCode);
				addrCountry = 37; // щом има ЕКАТТЕ е БГ
			} catch (Exception e) { // при нас е число
			}
		}		
		if (ekatte != null && !Objects.equals(address.getEkatte(), ekatte)) {
			changed = true;
			address.setEkatte(ekatte);
		}

		//
		String countryName = SearchUtils.trimToNULL(response.getCountryName());
		if (countryName != null) {
			List<SystemClassif> items = sd.getItemsByTekst(Constants.CODE_CLASSIF_COUNTRIES, countryName, SysConstants.CODE_LANG_BG, null);
			if (!items.isEmpty()) {
				addrCountry = items.get(0).getCode();
			}
		}
		if (addrCountry != null && !Objects.equals(address.getAddrCountry(), addrCountry)) {
			changed = true;
			address.setAddrCountry(addrCountry);
		}

		return changed;
	}

	/**
	 * Сетва в обекта Referent данните от RegIX. В данните от RegIX няма адреси и за това трябват да се ползва отделните методи за
	 * адреси
	 *
	 * @param referent
	 * @param response
	 * @return <code>true</code> ако има разлики от RegIX спрямо обекта, иначе <code>false</code>
	 * @see #setFzlReferentAddressData(ReferentAddress, PermanentAddressResponseType, BaseSystemData)
	 * @see #setFzlReferentAddressData(ReferentAddress, TemporaryAddressResponseType, BaseSystemData)
	 */
	public static boolean setFzlReferentData(Referent referent, PersonDataResponseType response) {
		if (response == null || response.getPersonNames() == null) {
			return false;
		}
		referent.setFzlEgn(response.getEGN()); // това е безусловно защото за нов запис трябва, а за корекция е еднакво

		boolean changed = false;

		//
		if (setPersonNames(referent, response.getPersonNames(), false)) {
			changed = true;
		}

		//
		if (setPersonNames(referent, response.getLatinNames(), true)) {
			changed = true;
		}

		//
		Date fzlBirthDate = DateUtils.toDate(response.getBirthDate());
		if (fzlBirthDate != null && !Objects.equals(referent.getFzlBirthDate(), fzlBirthDate)) {
			changed = true;
			referent.setFzlBirthDate(fzlBirthDate);
		}

		// 
		Date deathDate = DateUtils.toDate(response.getDeathDate());
		if (deathDate != null && !Objects.equals(referent.getDateSmart(), deathDate)) {
			changed = true;
			referent.setDateSmart(deathDate);
		}
		
		return changed;
	}

	/**
	 * Сетва в обекта Referent данните от RegIX. В данните от RegIX има всичко +адреса.
	 *
	 * @param referent
	 * @param response
	 * @param sd 
	 * @return <code>true</code> ако има разлики от RegIX спрямо обекта, иначе <code>false</code>
	 * @throws DbErrorException 
	 */
	public static boolean setNflReferentData(Referent referent, ActualStateResponseType response, BaseSystemData sd, LinkedHashMap<String, Object[]> diff) throws DbErrorException {
		if (response == null) {
			return false;
		}
		referent.setNflEik(response.getUIC()); // това е безусловно защото за нов запис трябва, а за корекция е еднакво

		if (referent.getAddress() == null) { // надолу ще трябва
			referent.setAddress(new ReferentAddress());
		}
		if (referent.getAddressKoresp() == null) {  // надолу ще трябва
			referent.setAddressKoresp(new ReferentAddress());
		}

		boolean changed = false;

		//
		if (!isStringEq(referent.getRefName(), response.getCompany())) {
			changed = true;
			diff.put("Наименование", new Object[] {referent.getRefName(), response.getCompany()});
			referent.setRefName(response.getCompany());
		}

		//
		if (!isStringEq(referent.getRefLatin(), response.getTransliteration())) {
			changed = true;
			diff.put("Наименование на латиница", new Object[] {referent.getRefLatin(), response.getTransliteration()});
			referent.setRefLatin(response.getTransliteration());
		}

		String liquidation = null;
		if (response.getLiquidationOrInsolvency() != null) {
			liquidation = response.getLiquidationOrInsolvency().value();
			
		} else if (response.getStatus() != null && response.getStatus().value() != null
				&& response.getStatus().value().toUpperCase().indexOf("ЗАКРИТА") != -1) {
			liquidation = response.getStatus().value();
		}
		if (!isStringEq(referent.getLiquidation(), liquidation)) {
			changed = true;
			diff.put("Ликвидация или несъстоятелност", new Object[] {referent.getLiquidation(), liquidation});
			referent.setLiquidation(liquidation);
		}

		//
		String predstavitelstvo = null;
		if (response.getDetails() != null 
				&& response.getDetails().getDetail() != null && !response.getDetails().getDetail().isEmpty()) {
			for (DetailType dt : response.getDetails().getDetail()) {
				if ("10а".equalsIgnoreCase(dt.getFieldCode()) && dt.getSubject() != null) {
					predstavitelstvo = dt.getSubject().getName();
					break;
				}
			}
		}
		if (!isStringEq(referent.getPredstavitelstvo(), predstavitelstvo)) {
			changed = true;
			diff.put("Представителство", new Object[] {referent.getPredstavitelstvo(), predstavitelstvo});
			referent.setPredstavitelstvo(predstavitelstvo);
		}

		//
		String contactEmail = null;
		String contactPhone = null;
		String webPage = null;
		if (response.getSeat() != null && response.getSeat().getContacts() != null) {
			contactEmail = response.getSeat().getContacts().getEMail();
			contactPhone = response.getSeat().getContacts().getPhone();
			webPage = response.getSeat().getContacts().getURL();
		}
		if (!isStringEq(referent.getContactEmail(), contactEmail)) {
			changed = true;
			diff.put("e-mail", new Object[] {referent.getContactEmail(), contactEmail});
			referent.setContactEmail(contactEmail);
		}
		if (!isStringEq(referent.getContactPhone(), contactPhone)) {
			changed = true;
			diff.put("Телефон", new Object[] {referent.getContactPhone(), contactPhone});
			referent.setContactPhone(contactPhone);
		}
		if (!isStringEq(referent.getWebPage(), webPage)) {
			changed = true;
			diff.put("Уеб страница", new Object[] {referent.getWebPage(), webPage});
			referent.setWebPage(webPage);
		}

		//
		ReferentAddress address = referent.getAddress();
		address.setAddrType(DocuConstants.CODE_ZNACHENIE_ADDR_TYPE_POSTOQNEN);
		AddressType seatForCorrespondence = response.getSeatForCorrespondence();
		changed |= setNflAddress(address, seatForCorrespondence, sd, diff, " (пост.)");

		//
		ReferentAddress addressKoresp = referent.getAddressKoresp();
		addressKoresp.setAddrType(DocuConstants.CODE_ZNACHENIE_ADDR_TYPE_CORRESP);
		AddressType addressType = null;
		if (response.getSeat() != null && response.getSeat().getAddress() != null) {
			addressType = response.getSeat().getAddress();
		}
		changed |= setNflAddress(addressKoresp, addressType, sd, diff, " (коресп.)");
		
//	    <ns2:Detail>
//           <ns2:FieldName>Представляващи</ns2:FieldName>
//           <ns2:FieldCode>10а</ns2:FieldCode>
//           <ns2:FieldOrder>00103</ns2:FieldOrder>
//           <ns2:Subject>
//               <ns2:Indent>7401107304</ns2:Indent>
//               <ns2:Name>Бони Атанасов Бончев</ns2:Name>
//               <ns2:IndentType>EGN</ns2:IndentType>
//           </ns2:Subject>
//       </ns2:Detail>

		return changed;
	}

	/**
	 * @param address
	 * @param addressType
	 * @param sd
	 * @param changed
	 * @return
	 * @throws DbErrorException
	 */
	private static boolean setNflAddress(ReferentAddress address, AddressType addressType, BaseSystemData sd, LinkedHashMap<String, Object[]> diff, String appendDiff) throws DbErrorException {
		Integer addrCountry = null;
		Integer ekatte = null;
		String postCode = null;
		StringBuilder addrText = new StringBuilder();

		if (addressType != null) {
			//
			String countryName = SearchUtils.trimToNULL(addressType.getCountry());
			if (countryName != null) {
				List<SystemClassif> items = sd.getItemsByTekst(Constants.CODE_CLASSIF_COUNTRIES, countryName, SysConstants.CODE_LANG_BG, null);
				if (!items.isEmpty()) {
					addrCountry = items.get(0).getCode();
				}
			}
			
			// 
			String ekatteCode = SearchUtils.trimToNULL(addressType.getSettlementEKATTE());
			if (ekatteCode != null) {
				try {
					ekatte = Integer.parseInt(ekatteCode);
				} catch (Exception e) { // при нас е число
				}
			}
			
			//
			postCode = addressType.getPostCode();
			
			//
			String t; // = SearchUtils.trimToNULL(addressType.getArea());
//			if (t != null) {
//				if (addrText.length() > 0) {
//					addrText.append(", ");
//				}
//				addrText.append(t);
//			}
			t = SearchUtils.trimToNULL(addressType.getHousingEstate());
			if (t != null) {
				if (addrText.length() > 0) {
					addrText.append(", ");
				}
				String tUp = t.toUpperCase();
				if (tUp.indexOf("Ж.") == -1) {
					addrText.append("ж.к. "); // ако няма изрично го добавям
				}
				addrText.append(t);
			}
			t = SearchUtils.trimToNULL(addressType.getStreet());
			if (t != null) {
				if (addrText.length() > 0) {
					addrText.append(", ");
				}
				String tUp = t.toUpperCase();
				if (ekatte != null && ekatte.intValue() == 68134 && (tUp.indexOf("ВАСИЛ ЛЕВСКИ") != -1 || tUp.indexOf("ЦАР БОРИС") != -1) && tUp.indexOf("БУЛ") == -1) {
					addrText.append("бул. ");
				} else if (tUp.indexOf("БУЛ") == -1 && tUp.indexOf("УЛ") == -1 && tUp.indexOf("Ж.К") == -1 && tUp.indexOf("ЖК") == -1 && tUp.indexOf("ПЛ") == -1) {
					addrText.append("ул. "); // ако няма изрично булевар или улица добавям улица
				}
				addrText.append(t);
			}
			t = SearchUtils.trimToNULL(addressType.getStreetNumber());
			if (t != null) {
				if (addrText.length() > 0) {
					addrText.append(" ");
				}
				addrText.append("№ " + t);
			}
			t = SearchUtils.trimToNULL(addressType.getBlock());
			if (t != null) {
				if (addrText.length() > 0) {
					addrText.append(", ");
				}
				addrText.append("бл. " + t);
			}
			t = SearchUtils.trimToNULL(addressType.getEntrance());
			if (t != null) {
				if (addrText.length() > 0) {
					addrText.append(", ");
				}
				addrText.append("вх. " + t);
			}
			t = SearchUtils.trimToNULL(addressType.getFloor());
			if (t != null) {
				if (addrText.length() > 0) {
					addrText.append(", ");
				}
				addrText.append("ет. " + t);
			}
			t = SearchUtils.trimToNULL(addressType.getApartment());
			if (t != null) {
				if (addrText.length() > 0) {
					addrText.append(", ");
				}
				addrText.append("ап. " + t);
			}
			t = SearchUtils.trimToNULL(addressType.getForeignPlace());
			if (t != null) {
				if (addrText.length() > 0) {
					addrText.append(", ");
				}
				addrText.append(" " + t);
			}
		}

		boolean changed = false;
		if (addrCountry != null && !Objects.equals(address.getAddrCountry(), addrCountry)) {
			changed = true;
			diff.put("Държава" + appendDiff, new Object[] {
					sd.decodeItem(Constants.CODE_CLASSIF_COUNTRIES, address.getAddrCountry(), 1, null)
					, sd.decodeItem(Constants.CODE_CLASSIF_COUNTRIES, addrCountry, 1, null)});
			address.setAddrCountry(addrCountry);
		}
		if (ekatte != null && !Objects.equals(address.getEkatte(), ekatte)) {
			changed = true;
			diff.put("ЕКАТТЕ" + appendDiff, new Object[] {address.getEkatte(), ekatte});
			address.setEkatte(ekatte);

			diff.put("Нас.Място" + appendDiff, new Object[] {
					sd.decodeItem(SysConstants.CODE_CLASSIF_EKATTE, address.getEkatte(), 1, null)
					, sd.decodeItem(SysConstants.CODE_CLASSIF_EKATTE, ekatte, 1, null)});
		}
		if (!isStringEq(address.getPostCode(), postCode)) {
			changed = true;
			diff.put("ПК" + appendDiff, new Object[] {address.getPostCode(), postCode});
			address.setPostCode(postCode);
		}
		if (!isStringEq(address.getAddrText(), addrText.toString())) {
			changed = true;
			diff.put("Адрес" + appendDiff, new Object[] {address.getAddrText(), addrText.toString()});
			address.setAddrText(addrText.toString());
		}
		return changed;
	}

	/**
	 * сглобява ги с разделител " ". Ако не са на латиница ги разпределя по новите колони
	 */
	private static boolean setPersonNames(Referent referent, PersonNames personNames, boolean latin) {
		if (personNames == null) {
			personNames = new PersonNames();  // за да не се бърка в логиката ако няма данни ще си сработи правилно
		}

		boolean changed = false;

		String ime = null;
		String prezime = null;
		String familia = null;
		
		StringBuilder names = new StringBuilder();
		if (personNames.getFirstName() != null) {
			if (names.length() > 0) {
				names.append(" ");
			}
			names.append(personNames.getFirstName());
			ime = String.valueOf(personNames.getFirstName());
		}
		if (personNames.getSurName() != null) {
			if (names.length() > 0) {
				names.append(" ");
			}
			names.append(personNames.getSurName());
			prezime = String.valueOf(personNames.getSurName());
		}
		if (personNames.getFamilyName() != null) {
			if (names.length() > 0) {
				names.append(" ");
			}
			names.append(personNames.getFamilyName());
			familia = String.valueOf(personNames.getFamilyName());
		}

		if (latin) {
			if (!isStringEq(referent.getRefLatin(), names.toString())) {
				changed = true;
				referent.setRefLatin(names.toString());
			}
		} else {
			if (!isStringEq(referent.getRefName(), names.toString())) {
				changed = true;
				referent.setRefName(names.toString());
			}

			if (!isStringEq(referent.getIme(), ime)) {
				changed = true;
				referent.setIme(ime);
			}
			if (!isStringEq(referent.getPrezime(), prezime)) {
				changed = true;
				referent.setPrezime(prezime);
			}
			if (!isStringEq(referent.getFamilia(), familia)) {
				changed = true;
				referent.setFamilia(familia);
			}
		}
		
		return changed;
	}
		
	
	/**
	 * Зареждане на данни за физическо лице от REGIX по подадено ЕГН.
	 * @param egn 
	 * @param loadPermanentAddress - дали искаме да се зарежда постоянния адрес
	 * @param loadCurrentAddress - дали искаме да се зарежда настоящ/кореспонденция адрес
	 * @return <code>true</code> ако има разлики от RegIX спрямо обекта, иначе <code>false</code> 
	 * @throws RegixClientException 
	 * @throws DbErrorException 
	 * @throws DatatypeConfigurationException 
	 */
	public static boolean loadFizLiceByEgn(Referent referent, String egn, boolean loadPermanentAddress, boolean loadCurrentAddress, SystemData sd) throws DbErrorException, RegixClientException, DatatypeConfigurationException {
		boolean changed=false;
		
		RegixClient client = sd.getRegixClient();
		
		// зареждаме основните данни
		PersonDataRequestType requestMainData = new PersonDataRequestType();
		requestMainData.setEGN(egn);
		PersonDataResponseType responseMainData = (PersonDataResponseType) client.executeOperation(GraoOperation.PERSON_DATA_SEARCH, requestMainData);
		
		if (responseMainData != null) {
			responseMainData.setEGN(egn); // заради тестовият регикс, защото го подменя
			changed |= setFzlReferentData(referent, responseMainData);			
		}
		
		// зареждане на постоянен адрес
		if (loadPermanentAddress) {
			PermanentAddressRequestType requestPermAddress = new PermanentAddressRequestType();
			requestPermAddress.setEGN(egn);
			requestPermAddress.setSearchDate(DateUtils.toGregorianCalendar(new Date()));

			PermanentAddressResponseType responsePermAddress = (PermanentAddressResponseType) client.executeOperation(GraoOperation.PERMANENT_ADDRESS_SEARCH, requestPermAddress);
			if (responsePermAddress != null) {
				if (referent.getAddress() == null) {
					referent.setAddress(new ReferentAddress());
				}
				changed |= setFzlReferentAddressData(referent.getAddress(), responsePermAddress, sd);
			}
		}
		
		// зареждане на настоящ адрес (адрес за кореспонденция)
		if (loadCurrentAddress) {
			TemporaryAddressRequestType requestCurrentAddress = new TemporaryAddressRequestType();
			requestCurrentAddress.setEGN(egn);
			requestCurrentAddress.setSearchDate(DateUtils.toGregorianCalendar(new Date()));
			
			TemporaryAddressResponseType responseCurrentAddress = (TemporaryAddressResponseType) client.executeOperation(GraoOperation.TEMPORARY_ADDRESS_SEARCH, requestCurrentAddress);
			if (responseCurrentAddress != null) {
				if (referent.getAddressKoresp() == null) {
					referent.setAddressKoresp(new ReferentAddress());
				}
				changed |= setFzlReferentAddressData(referent.getAddressKoresp(), responseCurrentAddress, sd);
			}
		}
		
		return changed;
	}
	
	/** 
	 * Зареждане на данни за юридическо лице от REGIX по подадено ЕИК.
	 * 
	 * @param referent
	 * @param eik
	 * @param sd
	 * @return
	 * @throws DbErrorException
	 * @throws RegixClientException
	 * @throws DatatypeConfigurationException
	 */
	public static boolean loadUridLiceByEik(Referent referent, String eik, SystemData sd) throws DbErrorException, RegixClientException, DatatypeConfigurationException {
		boolean changed=false;
		RegixClient client = sd.getRegixClient();
		
		
		ActualStateRequestType request = new ActualStateRequestType();
		request.setUIC(eik);

		ActualStateResponseType response = (ActualStateResponseType) client.executeOperation(TROperation.GET_ACTUAL_STATE, request);
		if (response!=null) {
			response.setUIC(eik); // заради тестовият регикс, защото го подменя
			changed=setNflReferentData(referent, response, sd, new LinkedHashMap<>());
		}
		
		return changed;
	}
	
	/** 
	 * Зареждане на данни за юридическо лице от REGIX по подадено ЕИК.
	 * 
	 * @param referent
	 * @param eik
	 * @param sd
	 * @return
	 * @throws DbErrorException
	 * @throws RegixClientException
	 * @throws DatatypeConfigurationException
	 */
	public static boolean loadUridLiceByEik(Referent referent, String eik, SystemData sd, LinkedHashMap<String, Object[]> diff) throws DbErrorException, RegixClientException, DatatypeConfigurationException {
		boolean changed=false;
		RegixClient client = sd.getRegixClient();
		
		ActualStateRequestType request = new ActualStateRequestType();
		request.setUIC(eik);

		ActualStateResponseType response = (ActualStateResponseType) client.executeOperation(TROperation.GET_ACTUAL_STATE, request);
		if (response!=null) {
			response.setUIC(eik); // заради тестовият регикс, защото го подменя
			changed=setNflReferentData(referent, response, sd, diff);
		}
		
		return changed;
	}
	
	/**
	 * Иска се ако от регикс дойде празно да се смята, че няма разлика и не се пипа текста при нас !!!
	 * 
	 * @return true ако са еднакви, като за еднакви се смятат "" и "   " еднаково на НУЛЛ !!!
	 */
	private static boolean isStringEq(String mms, String regix) {
		mms = SearchUtils.trimToNULL(mms);
		regix = SearchUtils.trimToNULL(regix);
		return regix == null || "-".equals(regix) || Objects.equals(mms, regix);
	}
	
}
