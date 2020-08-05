# pam-brukernotifikasjon
Applikasjon som holder publiserer brukernotifikasjoner knyttet til registrering av CV på Ditt NAV 

## Bakgrunn
Personer som kommer under oppfølging av NAV skal fylle ut CV på arbeidsplassen.no. For å syneliggjøre denne oppgaven publiseres varsel på Ditt NAV. 

Applikasjonen tar da imot beskjeder om personer som kommer under oppfølging og ikke lenger er under oppfølging, samt når personen redigerer CV. 

Disse opplysningene kan brukes for å bestemme om en person får en oppgave om å fylle ut CV.

Siden både CV og PTO (som leverer oppfølgingsinformasjon) primært benytter aktør-id og Ditt Nav benytter fødselsnummer, så må vi også mappe mellom disse identene - og trenger derfor persondataløsningen.

Applikasjonen orkestrer da input fra 3 kilder og produserer output til 1. Appen er bygd for å kunne gjøre dette asynkront. [CV-endringer mottas på kafka](https://github.com/navikt/pam-cv-avro-cvmeldinger), [Oppfølgingsdata fra feed](https://github.com/navikt/veilarboppfolging), [aktør-id/fødselsnummer-mapping fra PDL](https://navikt.github.io/pdl/) og det produseres [meldinger til Ditt NAV](https://github.com/navikt/brukernotifikasjon-topic-iac). 

## Bygge og starte applikasjonen lokalt
Bygge
```
gradle build
```

Starte i IntelliJ

Kjør `no.nav.cv.Application`, og legg på `-Dmicronaut.environments=test` som JVM option

## Admin-grensesnitt
Applikasjonen kan i teorien kjøre helt uten noe rest grensesnitt - kanskje uten readyness og liveness-probene til kubernetes/nais. Det finnes likevel et admin-grensesnitt for å kunne gjøre noen nytte operasjoner. Disse grensesnittene er feature-togglet av i prod ved harkodet verdi. Det kreves altså deploy å aktivere dem.

Siden testing er vanskelig - på grunn av at det er såpass mange integrasjoner (PTO og CV henger ikke nødvendigvis sammen, PDL ligger i et helt annet testmiljø), så er det en fordel å enkelt kunne legge inn fødselsnummer/aktør-id mapping. Det kan gjøres på `http://host:port/pam-brukernotifikasjon/internal/addIdent/{fnr}/{aktørid}`

Dersom det skulle være nødvendig å publisere noe på Ditt NAV manuelt er det også åpnet for å kunne gjøre det. En oppgave kan markeres som løst ved å sende en done-melding til Ditt NAV brukernotifikasjon `/internal/kafka/manuell/donemelding/${eventId}/${fødselsnummer}`

Dersom det har blitt sendt noen Done-meldinger uten varsler, så vil disse kunne forstyrre Ditt NAV ved at man til evig tid forsøker å finne oppgaven tilknyttet Done-meldingen. Det kan da sendes en manuelt ved å kalle endepunktet `/internal/kafka/manuell/varsel/{eventId}/{fødselsnummer}`
