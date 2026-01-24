const fs = require('fs')
const path = require('path')

let configPlugins
try {
  // eslint-disable-next-line import/no-extraneous-dependencies
  configPlugins = require('@expo/config-plugins')
} catch (e) {
  throw new Error(
    '`@whetware/react-native-stroke-text` Expo config plugin requires `@expo/config-plugins` (run via `expo prebuild`).'
  )
}

const {
  createRunOncePlugin,
  withDangerousMod,
  withGradleProperties,
} = configPlugins

function setGradleProperty(modResults, key, value) {
  const existing = modResults.find((item) => item.type === 'property' && item.key === key)
  if (existing) {
    existing.value = value
    return modResults
  }
  modResults.push({ type: 'property', key, value })
  return modResults
}

function withNewArchEnabledAndroid(config) {
  return withGradleProperties(config, (config) => {
    config.modResults = setGradleProperty(config.modResults, 'newArchEnabled', 'true')
    return config
  })
}

function withNewArchEnabledIos(config) {
  return withDangerousMod(config, [
    'ios',
    async (config) => {
      const iosRoot = config.modRequest.platformProjectRoot
      const propertiesPath = path.join(iosRoot, 'Podfile.properties.json')

      let json = {}
      if (fs.existsSync(propertiesPath)) {
        try {
          const contents = fs.readFileSync(propertiesPath, 'utf8')
          json = contents.trim().length ? JSON.parse(contents) : {}
        } catch {
          json = {}
        }
      }

      json.newArchEnabled = 'true'
      fs.writeFileSync(propertiesPath, JSON.stringify(json, null, 2) + '\n')
      return config
    },
  ])
}

function withStrokeText(config) {
  config = withNewArchEnabledAndroid(config)
  config = withNewArchEnabledIos(config)
  return config
}

const pkg = require('./package.json')

module.exports = createRunOncePlugin(withStrokeText, pkg.name, pkg.version)

