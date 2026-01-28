# Changesets

This repo uses [Changesets](https://github.com/changesets/changesets) for versioning and changelog management.

## Adding a changeset

On a PR that should result in a new release, add a changeset:

```sh
pnpm changeset
```

Commit the generated markdown file in `.changeset/`.

