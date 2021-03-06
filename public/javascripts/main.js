errorColor = "#FF0000"

/**
 * Vérification de l'équivalence des valeurs de deux champs
 * @param element1CssDescriptor Le descripteur css unique du champ 1
 * @param element2CssDescriptor Le descripteur css unique du champ 2
 * @param errorMessageElementDescriptor Le descripteur css unique du block de message d'erreur
 * @param errorMmessage Le message d'erreur à afficher
 * @returns {boolean} True si la verification est OK, false sinon
 */
function testEquivalence(element1CssDescriptor, element2CssDescriptor, errorMessageElementDescriptor, errorMmessage) {
    element1 = document.querySelector(element1CssDescriptor)
    element2 = document.querySelector(element2CssDescriptor)
    erroElement = document.querySelector(errorMessageElementDescriptor)
    if (element1 !== null && element1.value !== null && element1.value !== ""
        && element2 !== null && element2.value !== null && element2.value !== ""
        && element1.value !== element2.value) {
        element2.style.border = "thin solid " + errorColor;
        erroElement.innerHTML = errorMmessage
        erroElement.style.display = "block"
        return false;
    }
    else {
        element2.style.removeProperty('border')
        erroElement.style.display = "none"
        erroElement.innerHTML = ""
        return true;
    }
}

/**
 * Verification du formulaire d'un utilisateur , autre uqe les contrainte sur les élément html dans la page
 * @param messageErreurEmail Le message d'erreur de non equivalence des deux champs email
 * @param messageErreurPassword Le message d'erreur de non equivalence des deux champs mot de passe
 * @returns {boolean|*} True si la verification est Ok.
 */
function validateUserForm(messageErreurEmail, messageErreurPassword) {
    verificationEmail = testEquivalence("input#email", "input#emailControl", "div#emailsEqualityError", messageErreurEmail)
    if (document.querySelector("input#password") !== null) {
        verificationPassword = testEquivalence("input#password", "input#passwordUtilisateurContpermission", "div#passwordsEqualityError", messageErreurPassword)
    }
    else{
        verificationPassword = true;
    }
    return verificationEmail && verificationPassword;
}

$( "input#appId" ).autocomplete({
    source: function( req, response ) {
        $.getJSON( "@rcontrollers.user.routes.javascript.UserManagement.testAutoCompletion(\"\")" + extractLast( req.term ), { term: extractLast( req.term ) }, response );
    }
});

/**
 * Set the href value of the link "add permission to group"
 * @param link The link we want to set the href value
 * @param groupId The id of the group we want to add permission
 * @returns {boolean} True if the permissionId is defined , false else.
 */
function setHrefForAddPermissionToGroup(link,groupId){
    permissionId = document.querySelector("select#permissionToAdd").value;
    if(permissionId !== null && permissionId !== "") {
        link.href = "/group/addPermissionToGroup?groupId="+groupId+"&permissionId="+permissionId;
        return true;
    }
    return false;
}