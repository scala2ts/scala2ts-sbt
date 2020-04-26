/**
 * Invoke NPM to publish to an external registry
 */

(() => {
  const npm = require('npm');
  const ts = require('typescript');
  const tar = require('tar-fs');
  const meta = require('/Users/matt/Code/Couchmate/server/target/typescript/package.json');

  const body = tar.pack('/Users/matt/Code/Couchmate/server/target/typescript');

  npm.load(null, () => {
    npm.commands.publish(
      'https://gitlab.com/api/v4/projects/1/packages/npm/', {
        meta,
        auth: {
          token: '',
        },
        body,
      }, () => {
        console.log('Successfully published package to NPM registry');
      },
    );
  });
})();