/*
Aдаптор функция, на която се подава извикващата компонента, името на валидиращата функция,
минимална и максимална стойност (само за "isValidLength" и "isValidValue") и потребителско съобщение
*/
function commonValidation(validateFunctionName, thisComponent, secondComponentId, min, max, message){
	
	
	if (isNotBlank(validateFunctionName)){
//		alert("Не е подаден параметър 'валидираща функция!");
		console.log("Не е подаден параметър 'валидираща функция'!");
		return;
	}
	if (isNotBlank(thisComponent)){
//		alert("Не е подаден параметър 'извикващия компонент(this)'!");
		console.log("Не е подаден параметър 'извикващия компонент'(this)!");
		return;
	}
	
	var id = thisComponent.id;
	
	if (!isNotBlank(thisComponent.value)){
		
		var fnparams = [thisComponent.value];
		
		if (!isNotBlank(min)){
			fnparams.push(min);
		}
		if (!isNotBlank(max)){
			fnparams.push(max);
		}
		if (!isNotBlank(secondComponentId)){
			var fullSecCompId = thisComponent.form.name + ":" + secondComponentId;
			var secCompVal = document.getElementById(fullSecCompId).lastChild.value;
			fnparams.push(secCompVal);
		}
		
		var fn = window[validateFunctionName];
		var valid =	fn.apply(null, fnparams);
		
		var defaultMessage = "Default message";
		
		if (!isNotBlank(message)){
			defaultMessage = message;
		}
		if (!valid) {
			thisComponent.style.borderColor= 'red';
			PF('growlWV').renderMessage({"summary": defaultMessage,
	            "detail":"",
	            "severity":"error"});
			
		}else{
			thisComponent.style.borderColor= '#cccccc';
		}
	}else {
		thisComponent.style.borderColor= '#cccccc';
	}
	
}

function isNotBlank(val){
    return (val === undefined || val == null || val.length <= 0) ? true : false;
}

function isDate1GreaterThanDate2(date1, date2){
	
	var d1= new Date(date1);
	var d2 = new Date(date2);
	
	if (d1 > d2){
		return true;
	}else {
		return false;
	}
}

function isValidEGN(s) {

	var coeffs = [ 2, 4, 8, 5, 10, 9, 7, 3, 6 ];
	var digits = [];
	var days = [ 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 ];

	if (10 != s.length)
		return false;

	for (var i = 0; i < s.length; i++) {
		var digit = parseInt(s.charAt(i), 10);

		if (isNaN(digit)) {
			return false;
		}
		digits[i] = digit;

	}

	var dd = digits[4] * 10 + digits[5], mm = digits[2] * 10 + digits[3], yy = digits[0]
			* 10 + digits[1], yyyy = null;

	if (mm >= 1 && mm <= 12) {
		yyyy = 1900 + yy;
	} else if (mm >= 21 && mm <= 32) {
		mm -= 20;
		yyyy = 1800 + yy;
	} else if (mm >= 41 && mm <= 52) {
		mm -= 40;
		yyyy = 2000 + yy;
	}

	else
		return false;

	days[1] += isLeapYear(yyyy) ? 1 : 0;

	if (!(dd >= 1 && dd <= days[mm - 1]))
		return false;

	var checksum = 0;

	for (var j = 0; j < coeffs.length; j++) {
		checksum += digits[j] * coeffs[j];
	}

	checksum %= 11;

	if (10 == checksum) {
		checksum = 0;
	}

	if (digits[9] == checksum)
		return true;
	else
		return false;

}
function isLeapYear(year) {
	  return ((year % 4 === 0) && (year % 100 !== 0)) || (year % 400 === 0);
}

function isValidLNCH(s) {

	if (s.length == 10) {

		var tegla = [ 21, 19, 17, 13, 1, 9, 7, 3, 1 ];

		var sum = 0;

		for (var i = 0; i < 9; i++) { // Само цифри
			var ch = s.charAt(i);

			if (!(ch >= '0' && ch <= '9')) {
				return false;
			}
			sum += parseInt(ch) * tegla[i];

		}

		var os = sum % 10; // Остатък по модул 10
		if (10 == os) {
			os = 0;
		}
		if (s.charAt(9) == os) {
			return true; // Вярно контролно число
		}
	}
	return false;

}

function isValidBULSTAT(s) {

	var digits = [];
	var checksum = 0;
	var len = s.length;

	if (len != 9 && len != 13)
		return false;

	for (var i = 0; i < len; i++) {
		var digit = parseInt(s.charAt(i), 10);
		if (isNaN(digit)) {
			return false;
		}
		digits[i] = digit;
		if (i < 8) {
			checksum += digits[i] * (i + 1);
		}
	}

	checksum %= 11;

	if (checksum == 10) {
		checksum = 0;
		for (i = 0; i < 8; i++) {
			checksum += digits[i] * (i + 3);
		}
		checksum %= 11;
		if (checksum == 10) {
			checksum = 0;
		}
	}

	if (digits[8] != checksum)
		return false;

	if (len == 13) {
		checksum = 2 * digits[8] + 7 * digits[9] + 3 * digits[10] + 5
				* digits[11];
		checksum %= 11;
		if (checksum == 10) {
			checksum = 4 * digits[8] + 9 * digits[9] + 5 * digits[10] + 7
					* digits[11];
			checksum %= 11;
			if (checksum == 10) {
				checksum = 0;
			}
		}
		if (digits[12] != checksum)
			return false;
	}
	return true;
}

function isEmailValid(email) {
	var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
	return re.test(email);
}

function isDomainValid(domain) {
	var re = /^[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(\.[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*\.[a-zA-Z]{2,6}$/;
	return re.test(domain);
}
// Формат на датата ddMMyyyy
function isDate(dateStr) {
	
	var datePat = /^([0-9]{2})([0-9]{2})([0-9]{4})$/;
	var matchArray = dateStr.match(datePat);
	if (matchArray == null) {
		 
		return false;
	}
	day = dateStr.substring(0,2);
	month = dateStr.substring(2,4); // parse date into variables
	year = dateStr.substring(4,8);
	
	if (month < 1 || month > 12) { // check month range
//		alert("Month must be between 1 and 12");
		return false;
	}
	if (day < 1 || day > 31) {
//		alert("Day must be between 1 and 31");
		return false;
	}
	if ((month == 4 || month == 6 || month == 9 || month == 11) && day == 31) {
//		alert("Month " + month + " doesn't have 31 days!")
		return false;
	}
	if (month == 2) { // check for february 29th
		var isleap = (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0));
		if (day > 29 || (day == 29 && !isleap)) {
//			alert("February " + year + " doesn't have " + day + " days!");
			return false;
		}
	}
	return true; // date is valid
}

function isNumeric(n) {
	  return !isNaN(parseFloat(n)) && isFinite(n);
	}

function isBlank(str) {
    return !(!str || /^\s*$/.test(str));
}

function isLat(str) {
	var re = /^[a-zA-Z]+$/;
	return re.test(str);
}

function isBg(str) {
	var re = /^[а-яА-Я]+$/;
	return re.test(str);
}

function isBgWithDash(str) {
	var re = /^[а-яА-Я\\-]+$/;
	return re.test(str);
}

function isBgWithDashAndSpaces(str) {
	var re = /^[а-яА-Я \\-]+$/;
	return re.test(str);
}

function isBgWithDashSpacesAndComma(str) {
	var re = /^[а-яА-Я ,\\-]+$/;
	return re.test(str);
}

function isBigBg(str) {
	var re = /^[А-Я]+$/;
	return re.test(str);
}

function isBigBgWithDash(str) {
	var re = /^[А-Я\\-]+$/;
	return re.test(str);
}

function isBigBgWithDashAndSpace(str) {
	var re = /^[А-Я \\-]+$/;
	return re.test(str);
}

function isSmallBg(str) {
	var re = /^[а-я]+$/;
	return re.test(str);
}

function isSmallBgWithDash(str) {
	var re = /^[а-я\\-]+$/;
	return re.test(str);
}

function isSmallBgWithDashAndSpace(str) {
	var re = /^[а-я \\-]+$/;
	return re.test(str);
}
//ddMMyyyy
function isValidDateNotAfterToday(d) {
	if (isValidDate(d)) {


		var selectedDate = ParseDate(d);
		selectedDate.setHours(23, 59, 59, 59);
		var now = new Date();
		now.setHours(23,59,59,59);
		if (selectedDate.getTime() > now.getTime()) {
			return false;
		}
	}else{
		return false;
	}
	return true;
	
}
//ddMMyyyy
function isValidDateOnlyBeforeToday(d) {
	if (isValidDate(d)) {
		
		
		var selectedDate = ParseDate(d);
		
		var now = new Date();
		now.setHours(0,0,0,0);
		if (!(selectedDate.getTime() < now.getTime())) {
			return false;
		}
	}else{
		return false;
	}
	return true;
	
}
//ddMMyyyy
function isValidDateOnlyAfterToday(d) {
	if (isValidDate(d)) {
		
		
		var selectedDate = ParseDate(d);
		
		var now = new Date();
		now.setHours(23,59,59,59);
		if (!(selectedDate.getTime() > now.getTime())) {
			return false;
		}
	}else{
		return false;
	}
	return true;
	
}


function isLettersAndNumbers(str) {
	var re = /^[а-яА-Яa-zA-Z0-9]+$/;
	return re.test(str);
}

function isBigLettersAndNumbers(str) {
	var re = /^[А-ЯA-Z0-9]+$/;
	return re.test(str);
}

function isSmallLettersAndNumbers(str) {
	var re = /^[а-яa-z0-9]+$/;
	return re.test(str);
}

		//ddMMyyyy
function ParseDate(dateString) {
	var d=new Date();
	 
	var month=parseInt(dateString.substring(2,4),10);
	//mesecite sa ot 0 v js ... shto puk ne
	d.setFullYear(parseInt(dateString.substring(4,8)), month-1, parseInt(dateString.substring(0,2)));
	
    return d;
    
}

// Дали числото е валидно
function isValidValue (number, minValue, maxValue) {
	
	if (isNaN(number.trim())){
       // alert("Не е число!");
        return false;
	}
	
	if (!isNaN(parseFloat(minValue)) && isFinite(minValue)) {
		if (number.trim() < minValue) {
			//alert("Числото е по malko от min");
			return false;
		}
	}
	if (!isNaN(parseFloat(maxValue)) && isFinite(maxValue)) {
		if (number.trim() > maxValue) {
			//alert("Числото е по голямо от мах");
			return false;
		}
	}	
	//alert("Всичко е ОК");
	return true;
			
}

//дали е число
function isNumber (number){
	if (isNaN(number.trim())){
       // alert("Не е число!");
        return false;
	}
}

// Дали стринга е с валидена дължина
function isValidLength(str, minLength, maxLength) {
 	
	var length = str.trim().length; 
	if (!isNaN(parseFloat(minLength)) && isFinite(minLength)) {
		if (length < minLength) {
			//alert("Дължината на стринга е по малко от мин");
			return false;
		}
	}
    if (!isNaN(parseFloat(maxLength)) && isFinite(maxLength)) {
		if (length > maxLength) {
			//alert("Дължината на стринга е по голяма от мах");
			return false;	
		}
	}
    return true;
   // alert("Vsichko e ok");
}

// Проверка дали стрингът е цяло число.
function isValidValueInteger(str, min, max) {
	
	var re = /^-?[0-9]+$/;
	var valid = re.test(str) && Number.isInteger(+str);
	if(!valid) {
		return false;
	}
	
	return isValidValue(str, min, max);
}

