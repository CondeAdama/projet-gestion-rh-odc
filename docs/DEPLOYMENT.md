# Déploiement MINERVA GROUP RH

## Frontend (Netlify)

- URL de production : **https://minerva-rh.netlify.app**
- Variable build : `VITE_API_URL=https://minerva-rh-api.onrender.com/api/v2`

## Backend API (Render)

| Variable | Valeur recommandée |
|----------|-------------------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `MINERVA_APP_URL` | `https://minerva-rh.netlify.app` |
| `MINERVA_CORS_ORIGINS` | `https://minerva-rh.netlify.app,https://minerva-rh.onrender.com,http://localhost:5173` |

`MINERVA_APP_URL` alimente les liens d’activation, réinitialisation de mot de passe et SMS/e-mails. Si le dashboard Render contient encore l’ancienne URL du frontend statique (`minerva-rh.onrender.com`), les e-mails pointeront vers Render : corrigez cette variable ou laissez l’API réaligner l’URL en base au démarrage.

Dans **Configuration → Notifications**, le champ « URL de l’application » doit aussi être `https://minerva-rh.netlify.app`.

## Photos et logos

Les fichiers sont servis depuis le disque du conteneur API (`uploads/photos`, `uploads/logos`). Sur Render sans disque persistant, ils sont **perdus à chaque redéploiement** alors que les chemins restent en base : l’interface affiche alors les initiales. Pour une persistance durable, prévoir un stockage externe (S3, disque Render, etc.).
