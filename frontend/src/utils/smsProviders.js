/** Métadonnées des fournisseurs SMS — champs affichés dans Configuration */
export const SMS_PROVIDERS = [
  {
    value: 'TWILIO',
    label: 'Twilio',
    description: 'Leader mondial — crédit d\'essai puis payant.',
    fields: [
      { key: 'smsAccountSid', label: 'Account SID', placeholder: 'ACxxxxxxxxxxxxxxxx', required: true },
      { key: 'smsApiSecret', label: 'Auth Token', placeholder: '••••••••', secret: true, required: true },
      { key: 'smsSenderId', label: 'Numéro expéditeur (From)', placeholder: '+1234567890', required: true },
    ],
  },
  {
    value: 'BREVO',
    label: 'Brevo (Sendinblue)',
    description: 'Même compte que l\'e-mail — SMS transactionnels payants.',
    fields: [
      { key: 'smsApiSecret', label: 'Clé API Brevo (xkeysib-...)', placeholder: 'xkeysib-...', secret: true, required: true },
      { key: 'smsSenderId', label: 'Nom expéditeur SMS', placeholder: 'MINERVA GROUP', required: true },
    ],
  },
  {
    value: 'AFRICAS_TALKING',
    label: "Africa's Talking",
    description: 'Adapté à l\'Afrique — sandbox et production.',
    fields: [
      { key: 'smsAccountSid', label: 'Username', placeholder: 'sandbox ou nom compte', required: true },
      { key: 'smsApiSecret', label: 'API Key', placeholder: '••••••••', secret: true, required: true },
      { key: 'smsSenderId', label: 'Sender ID / From', placeholder: 'MINERVA', required: true },
    ],
  },
  {
    value: 'TERMII',
    label: 'Termii',
    description: 'Populaire en Afrique de l\'Ouest (Nigeria, etc.).',
    fields: [
      { key: 'smsApiSecret', label: 'API Key', placeholder: 'TL...', secret: true, required: true },
      { key: 'smsSenderId', label: 'Sender ID', placeholder: 'MINERVA', required: true },
      { key: 'smsExtra.channel', label: 'Canal (optionnel)', placeholder: 'generic', extra: true },
    ],
  },
  {
    value: 'VONAGE',
    label: 'Vonage (Nexmo)',
    description: 'API internationale — crédit d\'essai.',
    fields: [
      { key: 'smsAccountSid', label: 'API Key', placeholder: 'xxxxxxxx', required: true },
      { key: 'smsApiSecret', label: 'API Secret', placeholder: '••••••••', secret: true, required: true },
      { key: 'smsSenderId', label: 'From (numéro ou nom)', placeholder: 'MINERVA', required: true },
    ],
  },
  {
    value: 'MESSAGEBIRD',
    label: 'MessageBird',
    description: 'SMS, WhatsApp — API REST.',
    fields: [
      { key: 'smsApiSecret', label: 'Access Key (Live)', placeholder: '••••••••', secret: true, required: true },
      { key: 'smsSenderId', label: 'Originator', placeholder: 'MINERVA', required: true },
    ],
  },
  {
    value: 'AWS_SNS',
    label: 'Amazon SNS',
    description: 'AWS Simple Notification Service.',
    fields: [
      { key: 'smsAccountSid', label: 'Access Key ID', placeholder: 'AKIA...', required: true },
      { key: 'smsApiSecret', label: 'Secret Access Key', placeholder: '••••••••', secret: true, required: true },
      { key: 'smsExtra.region', label: 'Région AWS', placeholder: 'eu-west-1', extra: true, required: true },
      { key: 'smsSenderId', label: 'Sender ID (optionnel)', placeholder: 'MINERVA', required: false },
    ],
  },
  {
    value: 'CLICKSEND',
    label: 'ClickSend',
    description: 'SMS global — compte gratuit limité.',
    fields: [
      { key: 'smsAccountSid', label: 'Username', placeholder: 'votre@email.com', required: true },
      { key: 'smsApiSecret', label: 'API Key', placeholder: '••••••••', secret: true, required: true },
      { key: 'smsSenderId', label: 'From', placeholder: 'MINERVA', required: true },
    ],
  },
  {
    value: 'PLIVO',
    label: 'Plivo',
    description: 'SMS/voix — crédit d\'essai.',
    fields: [
      { key: 'smsAccountSid', label: 'Auth ID', placeholder: 'MAxxxxxxxx', required: true },
      { key: 'smsApiSecret', label: 'Auth Token', placeholder: '••••••••', secret: true, required: true },
      { key: 'smsSenderId', label: 'Numéro source', placeholder: '+1234567890', required: true },
    ],
  },
  {
    value: 'SINCH',
    label: 'Sinch',
    description: 'Ex-Clxcommunications — SMS entreprise.',
    fields: [
      { key: 'smsAccountSid', label: 'Service Plan ID', placeholder: 'xxxxxxxx-xxxx', required: true },
      { key: 'smsApiSecret', label: 'Bearer Token / API Token', placeholder: '••••••••', secret: true, required: true },
      { key: 'smsSenderId', label: 'From', placeholder: 'MINERVA', required: true },
    ],
  },
  {
    value: 'INFOBIP',
    label: 'Infobip',
    description: 'Plateforme CPaaS internationale.',
    fields: [
      { key: 'smsAccountSid', label: 'Base URL API', placeholder: 'https://xxxxx.api.infobip.com', required: true },
      { key: 'smsApiSecret', label: 'API Key', placeholder: '••••••••', secret: true, required: true },
      { key: 'smsSenderId', label: 'From', placeholder: 'MINERVA', required: true },
    ],
  },
  {
    value: 'TEXTLOCAL',
    label: 'Textlocal',
    description: 'SMS bulk — Inde, UK, international.',
    fields: [
      { key: 'smsApiSecret', label: 'API Key', placeholder: '••••••••', secret: true, required: true },
      { key: 'smsSenderId', label: 'Sender name', placeholder: 'MINERVA', required: true },
    ],
  },
  {
    value: 'TELESIGN',
    label: 'Telesign',
    description: 'Vérification et SMS transactionnels.',
    fields: [
      { key: 'smsAccountSid', label: 'Customer ID', placeholder: 'xxxxxxxx', required: true },
      { key: 'smsApiSecret', label: 'API Key', placeholder: '••••••••', secret: true, required: true },
      { key: 'smsSenderId', label: 'Message type', placeholder: 'ARN', required: false },
    ],
  },
  {
    value: 'HTTP',
    label: 'API HTTP personnalisée',
    description: 'Opérateur local ou gateway custom — POST JSON.',
    fields: [
      { key: 'smsAccountSid', label: 'URL de l\'API', placeholder: 'https://api.exemple.com/sms/send', required: true },
      { key: 'smsApiSecret', label: 'Token Bearer (optionnel)', placeholder: '••••••••', secret: true, required: false },
      { key: 'smsSenderId', label: 'Expéditeur (from)', placeholder: 'MINERVA-RH', required: true },
    ],
  },
];

export function getSmsProvider(value) {
  return SMS_PROVIDERS.find(p => p.value === value) || SMS_PROVIDERS[0];
}

export function getNestedValue(obj, path) {
  if (!path.includes('.')) return obj?.[path];
  const [root, ...rest] = path.split('.');
  return rest.reduce((acc, k) => acc?.[k], obj?.[root]);
}

export function setNestedValue(obj, path, value) {
  if (!path.includes('.')) return { ...obj, [path]: value };
  const [root, key] = path.split('.');
  return { ...obj, [root]: { ...(obj[root] || {}), [key]: value } };
}
