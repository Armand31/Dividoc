<div align="center">

# Dividoc

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?style=for-the-badge&logo=Gradle&logoColor=white)![Java](https://img.shields.io/badge/java-%2311C511.svg?style=for-the-badge&logo=java&logoColor=white)

![Testing Workflow](https://github.com/Armand31/Dividoc/actions/workflows/build.yml/badge.svg) ![Known Vulnerabilities](https://snyk.io/test/github/Armand31/Dividoc/badge.svg)
  
</div>

**Disaster Victim Documentation** is an Android app designed to assist with the documentation of dead bodies
in the field. The application produces a password-secured archive containing basic
information collected by the first responder, namely a set of pictures of the body associated
with its geolocation, as well as a unique identification tag allowing correlation with the
information registered during the burial/disposal process.

<p align="center">
<i>Screenshots of Dividoc, from left to right : Main menu, victim identification form and photo gallery view</i>
</p>
<p align="center">
  <img src="/documentation/images/main_menu.png" width="200" />
  <img src="/documentation/images/tag_form.png" width="200" /> 
  <img src="/documentation/images/photo_gallery_view.png" width="200" />
</p>

## Supervisors

** Armand Prioreschi** : Main developer and student at INSA Lyon, in telecommunications and networking. - [Contact : armand.prioreschi@insa-lyon.fr](mailto:armand.prioreschi@insa-lyon.fr)

**Jose Pablo Baraybar Do Carmo** : Conceiver of the project, Ph.D., anthropologist at the International Committee of the Red Cross (ICRC) - Paris Regional Delegation, Forensic Department - [Contact : jpbaraybar@icrc.org](mailto:jpbaraybar@icrc.org)

**Pierre François** : Student supervisor, Ph.D., researcher and professor at INSA Lyon - [Contact : pierre.francois@insa-lyon.fr](mailto:pierre.francois@insa-lyon.fr)

**Former collaborators** : The list of the former contributors is available in the [AUTHORS](AUTHORS) file.

## More about the project :

DiviX project is a set of tools used for victims identification in war, migration and disaster zones across the world.
Initiated at INSA Lyon in collaboration with the International Committee of the Red Cross (based in Geneva), the DiviX project is composed of :

- **Dividoc (Disaster Victim Documentation)** : An Android app (XML and Java) designed to assist with the documentation of dead bodies
  in the field. The application produces a password-secured archive containing basic
  information collected by the first responder, namely a set of pictures of the body associated
  with its geolocation, as well as a unique identification tag allowing correlation with the
  information registered during the burial/disposal process.

- **Divimap (Disaster Victim Mapping)** : An Android app (XML and Java) which records the case number prior to disposal and, like Dividoc, also produces geolocation and
  a password-secured archive of the pictures taken. The ultimate goal of the operation is to
  assist with victim traceability from discovery to disposal.

- **DiviX-Server** : A web server (NodeJS, VueJS, MySQL, Docker) that collects the data gathered by the Android applications and allows to filter and sort cases (victims) by age, sex, date or location, displays the cases location on a map (OpenStreetMap) and automatically extracts password-protected archives and populate the database. This sub-project is no longer maintained and was never deployed due to potential security issues that were not examined.
