# App zum speichern von Arbeitsstunden

Die App wurde entwickelt um zu speichern wann man als Trainer im Sport anwesend war, um den Lohn bestimmen zu können, kann jedoch für alles verwendet werden, bei dem feste Zeiten vorgegeben sind.

Derzeit befindet sich die App noch in Entwicklung und hat dementsprechend noch einige Fehler, jedoch sollten die wichtigen Features zuverlässig funktionieren.
Die App funktioniert ausschließlich auf Android, nicht IOS.


# Wie es funktioniert

Die regulären Arbeits-/Trainingszeiten, Zeiten zu denen man tatsächlich anwesend war und Abrechnungszeiträume werden in SQLite Datenbanken unverschlüsselt gespeichert. Zur Sicherheit werden diese regelmäßig per Google Autobackup gesichert. Alles was in den Einstellungen festgelegt wird wird **verschlüsselt** durch EncryptedSharedPreferences ausschließlich lokal gespeichert und nicht durch das backup gesichert.

# Wie installiere ich die App

Am einfachsten lässt sich die App mithilfe von Android Studio installieren. Dafür kann man einfach diesen Anweisungen folgen: https://developer.android.com/studio/run.

Ist die App sehr langsam wurde wahrscheinlich eine debuggable Version der App installiert. Für eine gute Performance sollte eine release Version installiert werden.
